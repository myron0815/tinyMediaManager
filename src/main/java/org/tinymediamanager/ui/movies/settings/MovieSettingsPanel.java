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
package org.tinymediamanager.ui.movies.settings;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.commons.lang3.StringUtils;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;
import org.tinymediamanager.Globals;
import org.tinymediamanager.core.Settings;
import org.tinymediamanager.core.movie.MovieModuleManager;
import org.tinymediamanager.core.movie.MovieNfoNaming;
import org.tinymediamanager.core.movie.connector.MovieConnectors;
import org.tinymediamanager.core.threading.TmmTask;
import org.tinymediamanager.core.threading.TmmTaskManager;
import org.tinymediamanager.scraper.trakttv.ClearTraktTvTask;
import org.tinymediamanager.ui.IconManager;
import org.tinymediamanager.ui.TmmFontHelper;
import org.tinymediamanager.ui.TmmUIHelper;
import org.tinymediamanager.ui.UTF8Control;
import org.tinymediamanager.ui.panels.ScrollablePanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

/**
 * The Class MovieSettingsPanel.
 * 
 * @author Manuel Laggner
 */
public class MovieSettingsPanel extends ScrollablePanel {
  private static final long           serialVersionUID = -7580437046944123496L;
  /**
   * @wbp.nls.resourceBundle messages
   */
  private static final ResourceBundle BUNDLE           = ResourceBundle.getBundle("messages", new UTF8Control()); //$NON-NLS-1$

  private Settings                    settings         = Settings.getInstance();
  private JComboBox<MovieConnectors>  cbNfoFormat;
  private JCheckBox                   cbMovieNfoFilename1;
  private JCheckBox                   cbMovieNfoFilename2;
  private JCheckBox                   cbMovieNfoFilename3;
  private JCheckBox                   chckbxMultipleMoviesPerFolder;
  private JCheckBox                   chckbxImageCache;
  private JTextField                  tfAddBadword;
  private JList<String>               listBadWords;
  private JList<String>               listDataSources;
  private JCheckBox                   chckbxYear;
  private JCheckBox                   chckbxTrailer;
  private JCheckBox                   chckbxSubtitles;
  private JCheckBox                   chckbxImages;
  private JCheckBox                   chckbxNfo;
  private JCheckBox                   chckbxRuntimeFromMf;
  private JCheckBox                   chckbxTraktTv;
  private JCheckBox                   chckbxWatched;
  private JCheckBox                   chckbxRating;
  private JCheckBox                   chckbxDateAdded;
  private JCheckBox                   chckbxSaveUiFilter;
  private JList<String>               listIgnore;

  /**
   * Instantiates a new movie settings panel.
   */
  public MovieSettingsPanel() {
    setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.LINE_GAP_ROWSPEC, RowSpec.decode("default:grow"), }));

    JPanel panelGeneral = new JPanel();
    panelGeneral.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.general"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelGeneral, "2, 2, fill, fill");
    panelGeneral.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.UNRELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, }));

    JLabel lblVisiblecolumns = new JLabel(BUNDLE.getString("Settings.movie.visiblecolumns")); //$NON-NLS-1$
    panelGeneral.add(lblVisiblecolumns, "2, 2, right, default");

    chckbxYear = new JCheckBox(BUNDLE.getString("metatag.year")); //$NON-NLS-1$
    panelGeneral.add(chckbxYear, "4, 2");

    chckbxRating = new JCheckBox(BUNDLE.getString("metatag.rating")); //$NON-NLS-1$
    panelGeneral.add(chckbxRating, "6, 2");

    chckbxNfo = new JCheckBox(BUNDLE.getString("metatag.nfo")); //$NON-NLS-1$
    panelGeneral.add(chckbxNfo, "8, 2");

    chckbxDateAdded = new JCheckBox(BUNDLE.getString("metatag.dateadded")); //$NON-NLS-1$
    panelGeneral.add(chckbxDateAdded, "10, 2");

    chckbxImages = new JCheckBox(BUNDLE.getString("metatag.images")); //$NON-NLS-1$
    panelGeneral.add(chckbxImages, "4, 4");

    chckbxTrailer = new JCheckBox(BUNDLE.getString("metatag.trailer")); //$NON-NLS-1$
    panelGeneral.add(chckbxTrailer, "6, 4");

    chckbxSubtitles = new JCheckBox(BUNDLE.getString("metatag.subtitles")); //$NON-NLS-1$
    panelGeneral.add(chckbxSubtitles, "8, 4");

    chckbxWatched = new JCheckBox(BUNDLE.getString("metatag.watched")); //$NON-NLS-1$
    panelGeneral.add(chckbxWatched, "10, 4");

    JLabel lblSaveUiFilter = new JLabel(BUNDLE.getString("Settings.movie.persistuifilter")); //$NON-NLS-1$
    panelGeneral.add(lblSaveUiFilter, "2, 6, right, default");

    chckbxSaveUiFilter = new JCheckBox("");
    panelGeneral.add(chckbxSaveUiFilter, "4, 6");

    JSeparator separator_4 = new JSeparator();
    panelGeneral.add(separator_4, "2, 8, 9, 1");

    JLabel lblImageCache = new JLabel(BUNDLE.getString("Settings.imagecacheimport"));
    panelGeneral.add(lblImageCache, "2, 10, right, default");

    chckbxImageCache = new JCheckBox(BUNDLE.getString("Settings.imagecacheimporthint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(chckbxImageCache, 0.833);
    panelGeneral.add(chckbxImageCache, "4, 10, 7, 1");

    JLabel lblRuntimeFromMedia = new JLabel(BUNDLE.getString("Settings.runtimefrommediafile"));
    panelGeneral.add(lblRuntimeFromMedia, "2, 12, right, default");

    chckbxRuntimeFromMf = new JCheckBox("");
    panelGeneral.add(chckbxRuntimeFromMf, "4, 12");

    JSeparator separator = new JSeparator();
    panelGeneral.add(separator, "2, 14, 9, 1");

    JLabel lblTraktTv = new JLabel(BUNDLE.getString("Settings.trakt"));//$NON-NLS-1$
    panelGeneral.add(lblTraktTv, "2, 16");

    chckbxTraktTv = new JCheckBox("");
    panelGeneral.add(chckbxTraktTv, "4, 16");

    JButton btnClearTraktTvMovies = new JButton(BUNDLE.getString("Settings.trakt.clearmovies"));//$NON-NLS-1$
    btnClearTraktTvMovies.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int confirm = JOptionPane.showOptionDialog(null, BUNDLE.getString("Settings.trakt.clearmovies.hint"),
            BUNDLE.getString("Settings.trakt.clearmovies"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null); //$NON-NLS-1$
        if (confirm == JOptionPane.YES_OPTION) {
          TmmTask task = new ClearTraktTvTask(true, false);
          TmmTaskManager.getInstance().addUnnamedTask(task);
        }
      }
    });
    panelGeneral.add(btnClearTraktTvMovies, "6, 16, 3, 1, left, default");

    JPanel panelMovieDataSources = new JPanel();

    panelMovieDataSources
        .setBorder(new TitledBorder(null, BUNDLE.getString("Settings.movie.datasource"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelMovieDataSources, "2, 4, 3, 1, fill, fill");
    panelMovieDataSources.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("50dlu:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.UNRELATED_GAP_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("200dlu:grow(2)"),
            FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, },
        new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.LABEL_COMPONENT_GAP_ROWSPEC, RowSpec.decode("100px:grow"),
            FormSpecs.UNRELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"), FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, }));

    JLabel lblDataSource = new JLabel(BUNDLE.getString("Settings.source")); //$NON-NLS-1$
    panelMovieDataSources.add(lblDataSource, "2, 2, 5, 1");

    JLabel lblIngore = new JLabel(BUNDLE.getString("Settings.ignore")); //$NON-NLS-1$
    panelMovieDataSources.add(lblIngore, "12, 2");

    JScrollPane scrollPaneDataSources = new JScrollPane();
    panelMovieDataSources.add(scrollPaneDataSources, "2, 4, 5, 1, fill, fill");

    listDataSources = new JList<>();
    scrollPaneDataSources.setViewportView(listDataSources);

    JPanel panelMovieSourcesButtons = new JPanel();
    panelMovieDataSources.add(panelMovieSourcesButtons, "8, 4, fill, top");
    panelMovieSourcesButtons.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JButton btnAdd = new JButton(IconManager.LIST_ADD);
    btnAdd.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAdd.setMargin(new Insets(2, 2, 2, 2));
    btnAdd.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.datasource.folderchooser")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isDirectory()) {
          settings.getMovieSettings().addMovieDataSources(file.getAbsolutePath());
        }
      }
    });

    panelMovieSourcesButtons.add(btnAdd, "1, 1, fill, top");

    JButton btnRemove = new JButton(IconManager.LIST_REMOVE);
    btnRemove.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemove.setMargin(new Insets(2, 2, 2, 2));
    btnRemove.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listDataSources.getSelectedIndex();
        if (row != -1) { // nothing selected
          String path = MovieModuleManager.MOVIE_SETTINGS.getMovieDataSource().get(row);
          String[] choices = { BUNDLE.getString("Button.continue"), BUNDLE.getString("Button.abort") }; //$NON-NLS-1$
          int decision = JOptionPane.showOptionDialog(null, String.format(BUNDLE.getString("Settings.movie.datasource.remove.info"), path),
              BUNDLE.getString("Settings.datasource.remove"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, choices,
              BUNDLE.getString("Button.abort")); //$NON-NLS-1$
          if (decision == 0) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MovieModuleManager.MOVIE_SETTINGS.removeMovieDataSources(path);
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
          }
        }
      }
    });
    panelMovieSourcesButtons.add(btnRemove, "1, 3, fill, top");

    JScrollPane scrollPaneIgnore = new JScrollPane();
    panelMovieDataSources.add(scrollPaneIgnore, "12, 4, fill, fill");

    listIgnore = new JList<>();
    scrollPaneIgnore.setViewportView(listIgnore);

    JPanel panelIgnoreButtons = new JPanel();
    panelMovieDataSources.add(panelIgnoreButtons, "14, 4, fill, fill");
    panelIgnoreButtons.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

    JButton btnAddIgnore = new JButton(IconManager.LIST_ADD);
    btnAddIgnore.setToolTipText(BUNDLE.getString("Settings.addignore")); //$NON-NLS-1$
    btnAddIgnore.setMargin(new Insets(2, 2, 2, 2));
    btnAddIgnore.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        File file = TmmUIHelper.selectDirectory(BUNDLE.getString("Settings.ignore")); //$NON-NLS-1$
        if (file != null && file.exists() && file.isDirectory()) {
          settings.getMovieSettings().addMovieSkipFolder(file.getAbsolutePath());
        }
      }
    });
    panelIgnoreButtons.add(btnAddIgnore, "1, 1");

    JButton btnRemoveIgnore = new JButton(IconManager.LIST_REMOVE);
    btnRemoveIgnore.setToolTipText(BUNDLE.getString("Settings.removeignore")); //$NON-NLS-1$
    btnRemoveIgnore.setMargin(new Insets(2, 2, 2, 2));
    btnRemoveIgnore.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int row = listIgnore.getSelectedIndex();
        if (row != -1) { // nothing selected
          String ingore = settings.getMovieSettings().getMovieSkipFolders().get(row);
          settings.getMovieSettings().removeMovieSkipFolder(ingore);
        }
      }
    });
    panelIgnoreButtons.add(btnRemoveIgnore, "1, 3");

    JLabel lblAllowMultipleMoviesPerFolder = new JLabel(BUNDLE.getString("Settings.multipleMovies")); //$NON-NLS-1$
    panelMovieDataSources.add(lblAllowMultipleMoviesPerFolder, "2, 6, right, default");

    chckbxMultipleMoviesPerFolder = new JCheckBox("");
    panelMovieDataSources.add(chckbxMultipleMoviesPerFolder, "4, 6");

    JTextPane tpMultipleMoviesHint = new JTextPane();
    TmmFontHelper.changeFont(tpMultipleMoviesHint, 0.833);
    tpMultipleMoviesHint.setBackground(UIManager.getColor("Panel.background"));
    tpMultipleMoviesHint.setText(BUNDLE.getString("Settings.multipleMovies.hint")); //$NON-NLS-1$
    tpMultipleMoviesHint.setEditable(false);
    panelMovieDataSources.add(tpMultipleMoviesHint, "6, 6, 9, 1, fill, fill");

    JSeparator separator_1 = new JSeparator();
    panelMovieDataSources.add(separator_1, "2, 8, 13, 1");

    JPanel panel = new JPanel();
    panelMovieDataSources.add(panel, "2, 10, 13, 1, fill, fill");
    panel.setLayout(new FormLayout(
        new ColumnSpec[] { FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC,
            ColumnSpec.decode("default:grow"), FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC, },
        new RowSpec[] { FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
            FormSpecs.DEFAULT_ROWSPEC, }));

    JLabel lblNfoFormat = new JLabel(BUNDLE.getString("Settings.nfoFormat"));
    panel.add(lblNfoFormat, "1, 1, right, default");

    cbNfoFormat = new JComboBox<>();
    panel.add(cbNfoFormat, "3, 1, fill, default");
    cbNfoFormat.setModel(new DefaultComboBoxModel<>(MovieConnectors.values()));

    JLabel lblNfoFileNaming = new JLabel(BUNDLE.getString("Settings.nofFileNaming")); //$NON-NLS-1$
    panel.add(lblNfoFileNaming, "1, 3, right, default");

    cbMovieNfoFilename1 = new JCheckBox(BUNDLE.getString("Settings.moviefilename") + ".nfo"); //$NON-NLS-1$
    panel.add(cbMovieNfoFilename1, "3, 3");

    cbMovieNfoFilename2 = new JCheckBox("movie.nfo");
    panel.add(cbMovieNfoFilename2, "3, 4");

    cbMovieNfoFilename3 = new JCheckBox(BUNDLE.getString("Settings.nfo.discstyle")); //$NON-NLS-1$
    panel.add(cbMovieNfoFilename3, "3, 5");
    cbMovieNfoFilename3.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });
    cbMovieNfoFilename2.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

    // item listener
    cbMovieNfoFilename1.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        checkChanges();
      }
    });

    JPanel panelBadWords = new JPanel();
    panelBadWords.setBorder(new TitledBorder(null, BUNDLE.getString("Settings.movie.badwords"), TitledBorder.LEADING, TitledBorder.TOP, null, null)); //$NON-NLS-1$
    add(panelBadWords, "4, 2, fill, fill");
    panelBadWords.setLayout(new FormLayout(
        new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("50px:grow"), FormFactory.RELATED_GAP_COLSPEC,
            FormFactory.DEFAULT_COLSPEC, },
        new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("default:grow"),
            FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

    JTextPane txtpntBadWordsHint = new JTextPane();
    txtpntBadWordsHint.setBackground(UIManager.getColor("Panel.background"));
    txtpntBadWordsHint.setText(BUNDLE.getString("Settings.movie.badwords.hint")); //$NON-NLS-1$
    TmmFontHelper.changeFont(txtpntBadWordsHint, 0.833);
    panelBadWords.add(txtpntBadWordsHint, "2, 2, 3, 1, fill, default");

    JScrollPane scpBadWords = new JScrollPane();
    panelBadWords.add(scpBadWords, "2, 4, fill, fill");

    listBadWords = new JList<>();
    scpBadWords.setViewportView(listBadWords);

    JButton btnRemoveBadWord = new JButton(IconManager.LIST_REMOVE);
    btnRemoveBadWord.setToolTipText(BUNDLE.getString("Button.remove")); //$NON-NLS-1$
    btnRemoveBadWord.setMargin(new Insets(2, 2, 2, 2));
    btnRemoveBadWord.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent arg0) {
        int row = listBadWords.getSelectedIndex();
        if (row != -1) {
          String badWord = MovieModuleManager.MOVIE_SETTINGS.getBadWords().get(row);
          MovieModuleManager.MOVIE_SETTINGS.removeBadWord(badWord);
        }
      }
    });
    panelBadWords.add(btnRemoveBadWord, "4, 4, default, bottom");

    tfAddBadword = new JTextField();
    tfAddBadword.setColumns(10);
    panelBadWords.add(tfAddBadword, "2, 6, fill, default");

    JButton btnAddBadWord = new JButton(IconManager.LIST_ADD);
    btnAddBadWord.setToolTipText(BUNDLE.getString("Button.add")); //$NON-NLS-1$
    btnAddBadWord.setMargin(new Insets(2, 2, 2, 2));
    btnAddBadWord.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (StringUtils.isNotEmpty(tfAddBadword.getText())) {
          MovieModuleManager.MOVIE_SETTINGS.addBadWord(tfAddBadword.getText());
          tfAddBadword.setText("");
        }
      }
    });
    panelBadWords.add(btnAddBadWord, "4, 6");

    initDataBindings();

    // NFO filenames
    List<MovieNfoNaming> movieNfoFilenames = settings.getMovieSettings().getMovieNfoFilenames();
    if (movieNfoFilenames.contains(MovieNfoNaming.FILENAME_NFO)) {
      cbMovieNfoFilename1.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.MOVIE_NFO)) {
      cbMovieNfoFilename2.setSelected(true);
    }
    if (movieNfoFilenames.contains(MovieNfoNaming.DISC_NFO)) {
      cbMovieNfoFilename3.setSelected(true);
    }

    if (!Globals.isDonator()) {
      chckbxTraktTv.setSelected(false);
      chckbxTraktTv.setEnabled(false);
      btnClearTraktTvMovies.setEnabled(false);
    }
  }

  /**
   * check changes of checkboxes
   */
  private void checkChanges() {
    // set NFO filenames
    settings.getMovieSettings().clearMovieNfoFilenames();
    if (cbMovieNfoFilename1.isSelected()) {
      settings.getMovieSettings().addMovieNfoFilename(MovieNfoNaming.FILENAME_NFO);
    }
    if (cbMovieNfoFilename2.isSelected()) {
      settings.getMovieSettings().addMovieNfoFilename(MovieNfoNaming.MOVIE_NFO);
    }
    if (cbMovieNfoFilename3.isSelected()) {
      settings.getMovieSettings().addMovieNfoFilename(MovieNfoNaming.DISC_NFO);
    }
  }

  /*
   * inti data bindings
   */
  @SuppressWarnings("rawtypes")
  protected void initDataBindings() {
    BeanProperty<Settings, MovieConnectors> settingsBeanProperty_10 = BeanProperty.create("movieSettings.movieConnector");
    BeanProperty<JComboBox<MovieConnectors>, Object> jComboBoxBeanProperty = BeanProperty.create("selectedItem");
    AutoBinding<Settings, MovieConnectors, JComboBox<MovieConnectors>, Object> autoBinding_9 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE,
        settings, settingsBeanProperty_10, cbNfoFormat, jComboBoxBeanProperty);
    autoBinding_9.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_2 = BeanProperty.create("movieSettings.detectMovieMultiDir");
    BeanProperty<JCheckBox, Boolean> jCheckBoxBeanProperty = BeanProperty.create("selected");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_2, chckbxMultipleMoviesPerFolder, jCheckBoxBeanProperty);
    autoBinding_2.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_3 = BeanProperty.create("movieSettings.buildImageCacheOnImport");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_3, chckbxImageCache, jCheckBoxBeanProperty);
    autoBinding_3.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_6 = BeanProperty.create("movieSettings.badWords");
    JListBinding<String, Settings, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings, settingsBeanProperty_6,
        listBadWords);
    jListBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_8 = BeanProperty.create("movieSettings.runtimeFromMediaInfo");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_6 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_8, chckbxRuntimeFromMf, jCheckBoxBeanProperty);
    autoBinding_6.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_9 = BeanProperty.create("movieSettings.yearColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_7 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_9, chckbxYear, jCheckBoxBeanProperty);
    autoBinding_7.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_13 = BeanProperty.create("movieSettings.trailerColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_8 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_13, chckbxTrailer, jCheckBoxBeanProperty);
    autoBinding_8.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_14 = BeanProperty.create("movieSettings.subtitleColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_12 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_14, chckbxSubtitles, jCheckBoxBeanProperty);
    autoBinding_12.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_15 = BeanProperty.create("movieSettings.imageColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_13 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_15, chckbxImages, jCheckBoxBeanProperty);
    autoBinding_13.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_16 = BeanProperty.create("movieSettings.nfoColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_14 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_16, chckbxNfo, jCheckBoxBeanProperty);
    autoBinding_14.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty = BeanProperty.create("movieSettings.syncTrakt");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty, chckbxTraktTv, jCheckBoxBeanProperty);
    autoBinding.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_1 = BeanProperty.create("movieSettings.watchedColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_1, chckbxWatched, jCheckBoxBeanProperty);
    autoBinding_1.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_5 = BeanProperty.create("movieSettings.ratingColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_5, chckbxRating, jCheckBoxBeanProperty);
    autoBinding_4.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_7 = BeanProperty.create("movieSettings.dateAddedColumnVisible");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_7, chckbxDateAdded, jCheckBoxBeanProperty);
    autoBinding_5.bind();
    //
    BeanProperty<Settings, Boolean> settingsBeanProperty_11 = BeanProperty.create("movieSettings.storeUiFilters");
    AutoBinding<Settings, Boolean, JCheckBox, Boolean> autoBinding_10 = Bindings.createAutoBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_11, chckbxSaveUiFilter, jCheckBoxBeanProperty);
    autoBinding_10.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_4 = BeanProperty.create("movieSettings.movieDataSource");
    JListBinding<String, Settings, JList> jListBinding_1 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_4, listDataSources);
    jListBinding_1.bind();
    //
    BeanProperty<Settings, List<String>> settingsBeanProperty_12 = BeanProperty.create("movieSettings.movieSkipFolders");
    JListBinding<String, Settings, JList> jListBinding_2 = SwingBindings.createJListBinding(UpdateStrategy.READ_WRITE, settings,
        settingsBeanProperty_12, listIgnore);
    jListBinding_2.bind();
  }
}
