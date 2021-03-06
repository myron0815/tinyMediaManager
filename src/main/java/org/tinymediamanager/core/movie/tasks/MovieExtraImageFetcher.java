/*
 * Copyright 2012 - 2015 Manuel Laggner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinymediamanager.core.movie.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.ImageCache;
import org.tinymediamanager.core.MediaFileType;
import org.tinymediamanager.core.Message;
import org.tinymediamanager.core.Message.MessageLevel;
import org.tinymediamanager.core.MessageManager;
import org.tinymediamanager.core.Utils;
import org.tinymediamanager.core.entities.MediaFile;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.entities.Movie;
import org.tinymediamanager.scraper.MediaArtwork.MediaArtworkType;
import org.tinymediamanager.scraper.http.Url;

/**
 * The class MovieExtraImageFetcher. To fetch extrafanarts and extrathumbs
 * 
 * @author Manuel Laggner
 */
public class MovieExtraImageFetcher implements Runnable {
  private final static Logger LOGGER = LoggerFactory.getLogger(MovieExtraImageFetcher.class);

  private Movie               movie;
  private MediaFileType       type;

  public MovieExtraImageFetcher(Movie movie, MediaFileType type) {
    this.movie = movie;
    this.type = type;
  }

  @Override
  public void run() {
    // try/catch block in the root of the thread to log crashes
    try {
      String base = ""; // single movie dir - no prefix
      if (movie.isMultiMovieDir()) {
        base = movie.getVideoBasenameWithoutStacking();
      }

      switch (type) {
        case LOGO:
        case BANNER:
        case CLEARART:
        case THUMB:
        case DISCART:
          downloadArtwork(type, base);
          break;
        default:
          break;
      }

      // just for single movies
      if (!movie.isMultiMovieDir()) {
        switch (type) {
          case EXTRATHUMB:
            downloadExtraThumbs();
            break;
          case EXTRAFANART:
            downloadExtraFanart();
            break;
          default:
            break;
        }
      }
      else {
        LOGGER.info("Movie '" + movie.getTitle() + "' is within a multi-movie-directory - skip downloading of " + type.name() + " images.");
      }

      // check if tmm has been shut down
      if (Thread.interrupted()) {
        return;
      }

      movie.callbackForWrittenArtwork(MediaArtworkType.ALL);
      movie.saveToDb();
    }
    catch (Exception e) {
      LOGGER.error("Thread crashed: ", e);
      MessageManager.instance.pushMessage(new Message(MessageLevel.ERROR, movie, "message.extraimage.threadcrashed"));
    }

  }

  private void downloadArtwork(MediaFileType type, String basename) {
    String artworkUrl = movie.getArtworkUrl(type);
    if (StringUtils.isBlank(artworkUrl)) {
      return;
    }

    String filename = basename;
    if (!filename.isEmpty()) {
      filename += "-";
    }

    try {
      String oldFilename = movie.getArtworkFilename(type);
      // we are lucky and have chosen our enums wisely - except the discart :(
      if (type == MediaFileType.DISCART) {
        filename += "disc." + FilenameUtils.getExtension(artworkUrl);
      }
      else {
        filename += type.name().toLowerCase() + "." + FilenameUtils.getExtension(artworkUrl);
      }
      movie.removeAllMediaFiles(type);

      // debug message
      LOGGER.debug("writing " + type + " " + filename);

      // fetch and store images
      Url url1 = new Url(artworkUrl);
      File tempFile = new File(movie.getPath(), filename + ".part");
      FileOutputStream outputStream = new FileOutputStream(tempFile);
      InputStream is = url1.getInputStream();
      IOUtils.copy(is, outputStream);
      outputStream.flush();
      try {
        outputStream.getFD().sync(); // wait until file has been completely written
        // give it a few milliseconds
        Thread.sleep(150);
      }
      catch (Exception e) {
        // empty here -> just not let the thread crash
      }
      outputStream.close();
      is.close();

      // has tmm been shut down?
      if (Thread.interrupted()) {
        return;
      }

      // check if the file has been downloaded
      if (!tempFile.exists() || tempFile.length() == 0) {
        throw new Exception("0byte file downloaded: " + filename);
      }

      // delete the old one if exisiting
      if (StringUtils.isNotBlank(oldFilename)) {
        File oldFile = new File(movie.getPath(), oldFilename);
        Utils.deleteFileSafely(oldFile);
      }

      // delete new destination if existing
      File destinationFile = new File(movie.getPath(), filename);
      Utils.deleteFileSafely(destinationFile);

      // move the temp file to the expected filename
      if (!Utils.moveFileSafe(tempFile, destinationFile)) {
        throw new Exception("renaming temp file failed: " + filename);
      }

      movie.setArtwork(destinationFile, type);
      movie.callbackForWrittenArtwork(MediaFileType.getMediaArtworkType(type));
      movie.saveToDb();
    }
    catch (Exception e) {
      if (e instanceof InterruptedException) {
        // only warning
        LOGGER.warn("interrupted image download");
      }
      else {
        LOGGER.error("fetch image: " + e.getMessage());
      }
      // remove temp file
      File tempFile = new File(movie.getPath(), filename + ".part");
      if (tempFile.exists()) {
        Utils.deleteFileSafely(tempFile);
      }
    }
  }

  private void downloadExtraFanart() {
    List<String> fanarts = movie.getExtraFanarts();

    // do not create extrafanarts folder, if no extrafanarts are selected
    if (fanarts.size() == 0) {
      return;
    }

    try {
      File folder = new File(movie.getPath(), "extrafanart");
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
        movie.removeAllMediaFiles(MediaFileType.EXTRAFANART);
      }
      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < fanarts.size(); i++) {
        String urlAsString = fanarts.get(i);
        String providedFiletype = FilenameUtils.getExtension(urlAsString);
        Url url = new Url(urlAsString);
        File file = new File(folder, "fanart" + (i + 1) + "." + providedFiletype);
        FileOutputStream outputStream = new FileOutputStream(file);
        InputStream is = url.getInputStream();
        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync(); // wait until file has been completely written
          // give it a few milliseconds
          Thread.sleep(150);
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();

        is.close();
        MediaFile mf = new MediaFile(file, MediaFileType.EXTRAFANART);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);
      }
    }
    catch (InterruptedException e) {
      LOGGER.warn("interrupted download extrafanarts");
    }
    catch (IOException e) {
      LOGGER.warn("download extrafanarts", e);
    }
  }

  private void downloadExtraThumbs() {
    List<String> thumbs = movie.getExtraThumbs();

    // do not create extrathumbs folder, if no extrathumbs are selected
    if (thumbs.size() == 0) {
      return;
    }

    try {
      File folder = new File(movie.getPath(), "extrathumbs");
      if (folder.exists()) {
        FileUtils.deleteDirectory(folder);
        movie.removeAllMediaFiles(MediaFileType.EXTRATHUMB);
      }
      folder.mkdirs();

      // fetch and store images
      for (int i = 0; i < thumbs.size(); i++) {
        String url = thumbs.get(i);
        String providedFiletype = FilenameUtils.getExtension(url);

        FileOutputStream outputStream = null;
        InputStream is = null;
        File file = null;
        if (MovieModuleManager.MOVIE_SETTINGS.isImageExtraThumbsResize() && MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsSize() > 0) {
          file = new File(folder, "thumb" + (i + 1) + ".jpg");
          outputStream = new FileOutputStream(file);
          try {
            is = ImageCache.scaleImage(url, MovieModuleManager.MOVIE_SETTINGS.getImageExtraThumbsSize());
          }
          catch (Exception e) {
            LOGGER.warn("problem with rescaling: " + e.getMessage());
            continue;
          }
        }
        else {
          file = new File(folder, "thumb" + (i + 1) + "." + providedFiletype);
          outputStream = new FileOutputStream(file);
          Url url1 = new Url(url);
          is = url1.getInputStream();
        }

        IOUtils.copy(is, outputStream);
        outputStream.flush();
        try {
          outputStream.getFD().sync();
        }
        catch (Exception e) {
          // empty here -> just not let the thread crash
        }
        outputStream.close();
        is.close();

        MediaFile mf = new MediaFile(file, MediaFileType.EXTRATHUMB);
        mf.gatherMediaInformation();
        movie.addToMediaFiles(mf);
      }
    }
    catch (IOException e) {
      LOGGER.warn("download extrathumbs", e);
    }
    catch (Exception e) {
      LOGGER.error(e.getMessage());
    }
  }
}
