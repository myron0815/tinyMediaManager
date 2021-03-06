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
package org.tinymediamanager.core.movie.connector;

import java.io.File;

/**
 * The Enum MovieConnectors.
 * 
 * @author Manuel Laggner
 */
public enum MovieConnectors {
  XBMC("Kodi / XBMC"), MP("MediaPortal");

  private String title;

  private MovieConnectors(String title) {
    this.title = title;
  }

  @Override
  public String toString() {
    return this.title;
  }

  /**
   * checks, if current NFO file is a valid XML<br>
   * (by casting to all known XML formats)
   * 
   * @param nfo
   * @return
   */
  public static boolean isValidNFO(File nfo) {
    MovieToXbmcNfoConnector tmp = null;
    try {
      tmp = MovieToXbmcNfoConnector.parseNFO(nfo);
    }
    catch (Exception e) {
    }

    MovieToMpNfoConnector tmp2 = null;
    if (tmp == null) {
      try {
        tmp2 = MovieToMpNfoConnector.parseNFO(nfo);
      }
      catch (Exception e) {
      }
    }

    if (tmp == null && tmp2 == null) {
      return false;
    }
    return true;
  }

}
