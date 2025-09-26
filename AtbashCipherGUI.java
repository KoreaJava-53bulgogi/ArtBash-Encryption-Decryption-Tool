import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Atbash Cipher를 위한 간단한 GUI 툴입니다.
 * 사용자는 텍스트를 입력하고 버튼을 눌러 암호화/복호화된 결과를 볼 수 있습니다.
 */
public class AtbashCipherGUI extends JFrame {

    private static class Theme {
        final Color background;
        final Color textAreaBg;
        final Color outputTextAreaBg;
        final Color foreground;
        final Color primaryButton;
        final Color secondaryButton;
        final Color border;
        final Color menuBarBg;
        final String themeName;

        public Theme(Color background, Color textAreaBg, Color outputTextAreaBg, Color foreground, Color primaryButton, Color secondaryButton, Color border, Color menuBarBg, String themeName) {
            this.background = background;
            this.textAreaBg = textAreaBg;
            this.outputTextAreaBg = outputTextAreaBg;
            this.foreground = foreground;
            this.primaryButton = primaryButton;
            this.secondaryButton = secondaryButton;
            this.border = border;
            this.menuBarBg = menuBarBg;
            this.themeName = themeName;
        }

        public static final Theme LIGHT = new Theme(
                new Color(245, 247, 249), // background
                Color.WHITE,              // textAreaBg
                new Color(235, 237, 239), // outputTextAreaBg
                new Color(50, 50, 50), // foreground
                new Color(0, 123, 255),   // primaryButton
                new Color(108, 117, 125), // secondaryButton
                new Color(220, 223, 226), // border
                Color.WHITE,               // menuBarBg
                "Light Theme"              // Theme Name
        );

        public static final Theme DARK = new Theme(
                new Color(43, 43, 43),    // background
                new Color(60, 63, 65),    // textAreaBg
                new Color(50, 52, 54),    // outputTextAreaBg
                new Color(220, 220, 220), // foreground
                new Color(2, 117, 216),   // primaryButton
                new Color(90, 98, 104),   // secondaryButton
                new Color(80, 80, 80),    // border
                Color.BLACK,               // menuBarBg
                "Dark Theme"              // Theme Name
        );
    }

    private enum ThemeMode {
        LIGHT, DARK
    }

    public enum Language {
        KOREAN, ENGLISH, CHINESE, JAPANESE, CUSTOM
    }

    private static final String APP_VERSION = "5.0";

    private JTextArea inputTextArea;
    private JTextArea outputTextArea;
    private JButton transformButton;
    private JButton clearButton;
    private JButton copyButton;
    private JLabel feedbackLabel;
    private MenuIcon saveIcon;
    private MenuIcon loadIcon;
    private MenuIcon saveImageIcon;
    private JComboBox<String> cipherSelector;
    private JLabel cipherLabel;
    private JPanel parameterPanel;
    private JLabel parameterLabel;
    private JSpinner parameterSpinner;
    private JLabel parameterLabel2;
    private JSpinner parameterSpinner2;
    private Timer feedbackTimer;
    private Timer animationTimer;

        private JMenuBar menuBar;
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JLabel inputLabel;
    private JLabel outputLabel;
    // Use custom RoundedScrollPane for rounded text areas
    private RoundedScrollPane inputScrollPane;
    private RoundedScrollPane outputScrollPane;
    private JMenu fileMenu;
    private JMenuItem saveMenuItem;
    private JMenuItem saveAsImageMenuItem;
    private JMenuItem loadMenuItem;
    private JCheckBoxMenuItem saveSettingsMenuItem;
    private JMenu themeMenu;
    private JRadioButtonMenuItem lightMenuItem;
    private JRadioButtonMenuItem darkMenuItem;
    private ButtonGroup themeGroup;
    private ButtonGroup languageGroup;
    private JMenu languageMenu;
    private JRadioButtonMenuItem koreanMenuItem;
    private JRadioButtonMenuItem englishMenuItem;
    private JRadioButtonMenuItem chineseMenuItem;
    private JRadioButtonMenuItem japaneseMenuItem;
    private JMenu helpMenu;
    private JMenuItem aboutMenuItem;
    private JMenu customThemesMenu;
    private JMenuItem saveAsNewThemeMenuItem;
    private JMenuItem deleteThemeMenuItem;
    private JMenuItem exportThemeMenuItem;
    private JMenuItem importThemeMenuItem;
    private JMenu customLanguageMenu;
    private JMenuItem createLanguagePackMenuItem;
    private JMenuItem deleteLanguagePackMenuItem;
    private JMenuItem exportLanguagePackMenuItem;
    private JMenuItem importLanguagePackMenuItem;

    private Language currentLanguage = Language.KOREAN;
    private LanguageManager languageManager;
    private boolean saveSettingsEnabled = true;
    private ActionListener cipherSaveListener;
    private Preferences prefs;

    public AtbashCipherGUI() {
        // Initialize preferences first, so they are available for initFrame()
        prefs = Preferences.userNodeForPackage(AtbashCipherGUI.class);
        languageManager = new LanguageManager();

        initFrame();
        initComponents();
        initLayout();
        initMenu();
        initListeners();
        loadSettings();
    }

    /**
     * A custom JButton with rounded corners and a hover effect.
     */
    private static class RoundedButton extends JButton {
        private final int cornerRadius;


        public RoundedButton(String text, int radius) {
            super(text);
            this.cornerRadius = radius;
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color color1 = getBackground();
            ButtonModel model = getModel();

            // Change colors based on button state
            if (model.isPressed()) {
                color1 = color1.darker();
            } else if (model.isRollover()) {
                color1 = color1.brighter();
            }

            // Use a single solid color for a flat look
            g2.setColor(color1);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();

            // Let the superclass paint the text and icon.
            super.paintComponent(g);
        }
    }

    /**
     * A custom JScrollPane with rounded corners.
     * The view component (e.g., JTextArea) should be set to non-opaque for this to work correctly.
     */
    private static class RoundedScrollPane extends JScrollPane {
        private final int cornerRadius;

        public RoundedScrollPane(Component view, int radius) {
            super(view);
            this.cornerRadius = radius;
            setOpaque(false);
            getViewport().setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw the rounded background using the text area's background color
            Color bgColor = getViewport().getView().getBackground();
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Draw a subtle border
            g2.setColor(new Color(0, 0, 0, 20)); // Semi-transparent black for a subtle shadow/border
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

            g2.dispose();

            // Let the original scrollpane paint its content (the text area)
            super.paintComponent(g);
        }
    }

    /**
     * A custom Icon implementation to draw menu icons programmatically.
     */
    private static class MenuIcon implements Icon {
        enum IconType { SAVE, LOAD, SAVE_IMAGE }

        private final IconType type;
        private Color color = Color.BLACK;
        private static final int ICON_SIZE = 16;

        public MenuIcon(IconType type) {
            this.type = type;
        }

        public void setColor(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(this.color);
            g2.setStroke(new BasicStroke(1.5f));

            if (type == IconType.SAVE) {
                // Downward arrow into a tray
                g2.drawLine(x + 8, y + 2, x + 8, y + 10); // Arrow shaft
                g2.drawLine(x + 5, y + 7, x + 8, y + 10); // Arrow left head
                g2.drawLine(x + 11, y + 7, x + 8, y + 10); // Arrow right head
                // Tray
                g2.drawLine(x + 2, y + 13, x + 14, y + 13); // Bottom
                g2.drawLine(x + 2, y + 10, x + 2, y + 13); // Left side
                g2.drawLine(x + 14, y + 10, x + 14, y + 13); // Right side
            } else if (type == IconType.LOAD) {
                // Simple folder icon
                g2.drawRect(x + 1, y + 3, ICON_SIZE - 3, ICON_SIZE - 5); // Folder back
                g2.drawPolyline(new int[]{x, x, x + 5, x + 7, x + ICON_SIZE - 1, x + ICON_SIZE - 1},
                                new int[]{y + ICON_SIZE - 2, y + 5, y + 5, y + 3, y + 3, y + ICON_SIZE - 2}, 6); // Folder front
            } else if (type == IconType.SAVE_IMAGE) {
                // Picture frame icon
                g2.drawRect(x + 2, y + 2, ICON_SIZE - 5, ICON_SIZE - 5);
                // Sun
                g2.drawOval(x + 4, y + 4, 3, 3);
                // Mountains
                g2.drawPolyline(new int[]{x + 3, x + 6, x + 9, x + 12}, new int[]{y + 12, y + 8, y + 10, y + 12}, 4);
            }
            g2.dispose();
        }

        @Override public int getIconWidth() { return ICON_SIZE; }
        @Override public int getIconHeight() { return ICON_SIZE; }
    }

    private void loadSettings() {
        // Temporarily disable the listener while settings are being loaded
        // to prevent programmatic changes from triggering a save.
        if (cipherSaveListener != null) {
            cipherSelector.removeActionListener(cipherSaveListener);
        }

        // Load "save settings" toggle state first. This one is always loaded.
        saveSettingsEnabled = prefs.getBoolean("saveSettingsEnabled", true);
        if (saveSettingsMenuItem != null) {
            saveSettingsMenuItem.setSelected(saveSettingsEnabled);
        }

        // Load language, default to KOREAN
        String langName = prefs.get("language", Language.KOREAN.name());
        Language language = Language.KOREAN;
        try { language = Language.valueOf(langName); } catch (IllegalArgumentException e) { /* Use default */ }
        // Set language first, as it affects UI text.
        setLanguage(language);

        // Load custom theme names and rebuild the menu
        List<String> customThemeNames = getCustomThemeNames();
        rebuildCustomThemesMenu(customThemeNames);

        // Load custom language pack names and rebuild the menu
        List<String> customLangNames = getCustomLanguagePackNames();
        rebuildCustomLanguageMenu(customLangNames);

        // Load last used theme
        String themeName = prefs.get("theme", ThemeMode.LIGHT.name());
        if (themeName.equals(ThemeMode.LIGHT.name())) {
            setTheme(ThemeMode.LIGHT);
        } else if (themeName.equals(ThemeMode.DARK.name())) {
            setTheme(ThemeMode.DARK);
        } else if (customThemeNames.contains(themeName)) {
            // It's a valid custom theme
            if (saveSettingsEnabled) {
                applyCustomTheme(themeName);
                // Find and select the radio button for the loaded theme
                for (Component item : customThemesMenu.getMenuComponents()) {
                    if (item instanceof JRadioButtonMenuItem && themeName.equals(item.getName())) {
                        ((JRadioButtonMenuItem) item).setSelected(true);
                        break;
                    }
                }
            } else {
                // If settings are not saved, just load the light theme
                setTheme(ThemeMode.LIGHT);
            }
        } else {
            // Fallback to light theme if the saved theme name is not found
            setTheme(ThemeMode.LIGHT);
        }

        // Apply custom language if it was the last used one
        if (language == Language.CUSTOM) {
            String customLangName = prefs.get("customLanguageName", "");
            if (!customLangName.isEmpty() && customLangNames.contains(customLangName)) {
                applyCustomLanguage(customLangName);
                // Select the radio button for the loaded language
                selectCustomLanguageMenuItem(customLangName);
            }
        } else {
            languageManager.loadLanguage(language);
        }

        // Load cipher mode, default to 0 (Atbash)
        int cipherIndex = prefs.getInt("cipherMode", 0);
        if (cipherIndex >= 0 && cipherIndex < cipherSelector.getItemCount()) {
            cipherSelector.setSelectedIndex(cipherIndex);
        }

        // Manually trigger the UI update for the parameter panel based on the loaded cipher
        updateParameterPanel(cipherSelector.getSelectedIndex());

        // Re-enable the listener
        if (cipherSaveListener != null) {
            cipherSelector.addActionListener(cipherSaveListener);
        }
    }

    private void updateParameterPanel(int selectedIndex) {
        boolean showPanel = false;
        // Default to hiding second parameter
        parameterLabel2.setVisible(false);
        parameterSpinner2.setVisible(false);

        if (selectedIndex >= 3 && selectedIndex <= 4) { // Caesar
            parameterLabel.setText(languageManager.get("PARAM_SHIFT_KEY"));
            parameterSpinner.setModel(new SpinnerNumberModel(prefs.getInt("caesarShift", 3), -1000, 1000, 1));
            showPanel = true;
        } else if (selectedIndex >= 5 && selectedIndex <= 6) { // Scytale
            parameterLabel.setText(languageManager.get("PARAM_DIAMETER"));
            parameterSpinner.setModel(new SpinnerNumberModel(prefs.getInt("scytaleDiameter", 5), 1, 1000, 1));
            showPanel = true;
        } else if (selectedIndex >= 7 && selectedIndex <= 8) { // Chained ciphers
            // First parameter: Scytale Diameter
            parameterLabel.setText(languageManager.get("PARAM_DIAMETER"));
            parameterSpinner.setModel(new SpinnerNumberModel(prefs.getInt("scytaleDiameter", 5), 1, 1000, 1));
            // Second parameter: Caesar Shift
            parameterLabel2.setText(languageManager.get("PARAM_SHIFT_KEY"));
            parameterSpinner2.setModel(new SpinnerNumberModel(prefs.getInt("caesarShift", 3), -1000, 1000, 1));
            parameterLabel2.setVisible(true);
            parameterSpinner2.setVisible(true);
            showPanel = true;
        }

        parameterPanel.setVisible(showPanel);
        if (parameterPanel != null && parameterPanel.getParent() != null) {
            parameterPanel.getParent().revalidate();
            parameterPanel.getParent().repaint();
        }
    }

    private void initFrame() {
        // Title is set by applyLanguage()
        // Try to load multiple icon sizes for the best appearance across different OS contexts.
        try {
            List<String> iconFileNames = List.of("icon-16.png", "icon-32.png", "icon-48.png", "icon-64.png");
            List<Image> icons = new ArrayList<>();

            for (String fileName : iconFileNames) {
                URL iconURL = getClass().getResource(fileName);
                if (iconURL != null) {
                    icons.add(new ImageIcon(iconURL).getImage());
                }
            }

            if (!icons.isEmpty()) {
                // Set multiple icon sizes for the frame. The OS will choose the best one.
                setIconImages(icons);
            } else {
                System.err.println("Warning: No application icons found (e.g., 'icon-32.png').");
            }
        } catch (Exception e) {
            System.err.println("Error loading app icon: " + e.getMessage());
        }

        // Load window geometry from preferences
        int width = prefs.getInt("windowWidth", 1100);
        int height = prefs.getInt("windowHeight", 500);

        setSize(width, height);
        setMinimumSize(new Dimension(700, 400));

        // Always center the window on startup
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
 
    private void initMenu() {
        menuBar = new JMenuBar();

        fileMenu = new JMenu();
        saveMenuItem = new JMenuItem();
        saveAsImageMenuItem = new JMenuItem();
        saveMenuItem.setIcon(saveIcon);
        saveAsImageMenuItem.setIcon(saveImageIcon);
        loadMenuItem = new JMenuItem();
        loadMenuItem.setIcon(loadIcon);
        saveSettingsMenuItem = new JCheckBoxMenuItem();
        saveSettingsMenuItem.setSelected(true);
        fileMenu.add(loadMenuItem);
        fileMenu.add(saveSettingsMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(saveMenuItem);
        fileMenu.add(saveAsImageMenuItem);

        themeMenu = new JMenu();
        themeGroup = new ButtonGroup();

        lightMenuItem = new JRadioButtonMenuItem();
        lightMenuItem.setSelected(true);
        lightMenuItem.addActionListener(e -> setTheme(ThemeMode.LIGHT));

        darkMenuItem = new JRadioButtonMenuItem();
        darkMenuItem.addActionListener(e -> setTheme(ThemeMode.DARK));

        themeGroup.add(lightMenuItem);
        themeGroup.add(darkMenuItem);
        themeMenu.add(lightMenuItem);

        themeMenu.add(darkMenuItem);
        themeMenu.addSeparator();

        customThemesMenu = new JMenu();
        themeMenu.add(customThemesMenu);
        themeMenu.addSeparator();

        saveAsNewThemeMenuItem = new JMenuItem();
        deleteThemeMenuItem = new JMenuItem();
        themeMenu.add(saveAsNewThemeMenuItem);
        themeMenu.add(deleteThemeMenuItem);
        themeMenu.addSeparator();

        exportThemeMenuItem = new JMenuItem();
        importThemeMenuItem = new JMenuItem();
        themeMenu.add(exportThemeMenuItem);
        themeMenu.add(importThemeMenuItem);

        languageMenu = new JMenu();
        languageGroup = new ButtonGroup();

        koreanMenuItem = new JRadioButtonMenuItem();
        koreanMenuItem.setSelected(true);
        koreanMenuItem.addActionListener(e -> setLanguage(Language.KOREAN));

        englishMenuItem = new JRadioButtonMenuItem();
        englishMenuItem.addActionListener(e -> setLanguage(Language.ENGLISH));

        chineseMenuItem = new JRadioButtonMenuItem();
        chineseMenuItem.setName("chineseMenuItem");
        chineseMenuItem.addActionListener(e -> setLanguage(Language.CHINESE));

        japaneseMenuItem = new JRadioButtonMenuItem();
        japaneseMenuItem.addActionListener(e -> setLanguage(Language.JAPANESE));

        languageGroup.add(koreanMenuItem);
        languageGroup.add(englishMenuItem);

        languageGroup.add(chineseMenuItem);
        languageGroup.add(japaneseMenuItem);
        languageMenu.add(koreanMenuItem);
        languageMenu.add(englishMenuItem);
        languageMenu.add(chineseMenuItem);
        languageMenu.add(japaneseMenuItem);

        languageMenu.addSeparator();
        customLanguageMenu = new JMenu();
        languageMenu.add(customLanguageMenu);
        languageMenu.addSeparator();
        createLanguagePackMenuItem = new JMenuItem();
        deleteLanguagePackMenuItem = new JMenuItem();
        exportLanguagePackMenuItem = new JMenuItem();
        importLanguagePackMenuItem = new JMenuItem();
        languageMenu.add(createLanguagePackMenuItem);
        languageMenu.add(deleteLanguagePackMenuItem);
        languageMenu.add(exportLanguagePackMenuItem);
        languageMenu.add(importLanguagePackMenuItem);

        helpMenu = new JMenu();

        aboutMenuItem = new JMenuItem();
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(languageMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
    }

    private void promptAndSaveNewTheme() {
        // Create a panel for the customization options
        JPanel customThemePanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        customThemePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get current colors to initialize the pickers
        Color currentMainBg = mainPanel.getBackground();
        Color currentTextAreaBg = inputTextArea.getBackground();
        Color currentTextColor = inputTextArea.getForeground();
        Color currentPrimaryBtn = transformButton.getBackground();
        Color currentSecondaryBtn = clearButton.getBackground();

        // Create color chooser panels
        JPanel mainBgPanel = createColorPickerPanel(currentMainBg);
        JPanel textAreaBgPanel = createColorPickerPanel(currentTextAreaBg);
        JPanel textColorPanel = createColorPickerPanel(currentTextColor);
        JPanel primaryBtnPanel = createColorPickerPanel(currentPrimaryBtn);
        JPanel secondaryBtnPanel = createColorPickerPanel(currentSecondaryBtn);

        // Add components to the panel with localized labels
        customThemePanel.add(new JLabel(languageManager.get("CUSTOMIZE_THEME_MAIN_BG")));
        customThemePanel.add(mainBgPanel);
        customThemePanel.add(new JLabel(languageManager.get("CUSTOMIZE_THEME_TEXT_AREA_BG")));
        customThemePanel.add(textAreaBgPanel);
        customThemePanel.add(new JLabel(languageManager.get("CUSTOMIZE_THEME_TEXT_COLOR")));
        customThemePanel.add(textColorPanel);
        customThemePanel.add(new JLabel(languageManager.get("CUSTOMIZE_THEME_PRIMARY_BTN")));
        customThemePanel.add(primaryBtnPanel);
        customThemePanel.add(new JLabel(languageManager.get("CUSTOMIZE_THEME_SECONDARY_BTN")));
        customThemePanel.add(secondaryBtnPanel);

        int result = JOptionPane.showConfirmDialog(AtbashCipherGUI.this,
                customThemePanel,
                languageManager.get("CUSTOMIZE_THEME_DIALOG_TITLE"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // Retrieve the new colors from the panels
            Color newMainBg = mainBgPanel.getBackground();
            Color newTextAreaBg = textAreaBgPanel.getBackground();
            Color newTextColor = textColorPanel.getBackground();
            Color newPrimaryBtn = primaryBtnPanel.getBackground();
            Color newSecondaryBtn = secondaryBtnPanel.getBackground();

            // Prompt for a theme name
            String themeName = JOptionPane.showInputDialog(this,
                    languageManager.get("ENTER_THEME_NAME_MESSAGE"),
                    languageManager.get("ENTER_THEME_NAME_TITLE"),
                    JOptionPane.PLAIN_MESSAGE);

            if (themeName != null && !themeName.isBlank()) {
                themeName = themeName.trim();
                if (themeName.equalsIgnoreCase("LIGHT") || themeName.equalsIgnoreCase("DARK") || themeName.equalsIgnoreCase("CUSTOM")) {
                    JOptionPane.showMessageDialog(this, languageManager.get("INVALID_THEME_NAME_MESSAGE"), languageManager.get("INVALID_THEME_NAME_TITLE"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save the new theme's colors
                prefs.putInt("custom.theme." + themeName + ".main.background", newMainBg.getRGB());
                prefs.putInt("custom.theme." + themeName + ".textarea.background", newTextAreaBg.getRGB());
                prefs.putInt("custom.theme." + themeName + ".text.foreground", newTextColor.getRGB());
                prefs.putInt("custom.theme." + themeName + ".button.primary", newPrimaryBtn.getRGB());
                prefs.putInt("custom.theme." + themeName + ".button.secondary", newSecondaryBtn.getRGB());

                // Update the list of theme names
                List<String> names = getCustomThemeNames();
                if (!names.contains(themeName)) {
                    names.add(themeName);
                    saveCustomThemeNames(names);
                    rebuildCustomThemesMenu(names);
                }

                // Apply and select the new theme
                applyCustomTheme(themeName);
                for (Component item : customThemesMenu.getMenuComponents()) {
                    if (item instanceof JRadioButtonMenuItem && themeName.equals(item.getName())) {
                        ((JRadioButtonMenuItem) item).setSelected(true);
                        break;
                    }
                }
            } else if (themeName != null) { // User entered blank name
                JOptionPane.showMessageDialog(this, languageManager.get("INVALID_THEME_NAME_MESSAGE"), languageManager.get("INVALID_THEME_NAME_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createColorPickerPanel(Color initialColor) {
        JPanel colorPanel = new JPanel();
        colorPanel.setBackground(initialColor);
        colorPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        colorPanel.setPreferredSize(new Dimension(100, 25));
        colorPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                String title = languageManager.get("CUSTOMIZE_THEME_COLOR_CHOOSER_TITLE");
                Color newColor = JColorChooser.showDialog(AtbashCipherGUI.this, title, colorPanel.getBackground());
                if (newColor != null) {
                    colorPanel.setBackground(newColor);
                }
            }
        });
        return colorPanel;
    }

    private void setTheme(ThemeMode mode) {
        // Update radio button selection state
        lightMenuItem.setSelected(mode == ThemeMode.LIGHT);
        darkMenuItem.setSelected(mode == ThemeMode.DARK);

        // Save the preference
        if (saveSettingsEnabled) {
            prefs.put("theme", mode.name());
        }

        Theme themeToApply = (mode == ThemeMode.DARK) ? Theme.DARK : Theme.LIGHT;
        applyTheme(themeToApply);
    }

    private void setLanguage(Language lang) {
        this.currentLanguage = lang;

        koreanMenuItem.setSelected(lang == Language.KOREAN);
        englishMenuItem.setSelected(lang == Language.ENGLISH);
        chineseMenuItem.setSelected(lang == Language.CHINESE);
        japaneseMenuItem.setSelected(lang == Language.JAPANESE);
        if (lang != Language.CUSTOM) {
            for (Component item : customLanguageMenu.getMenuComponents()) {
                if (item instanceof JRadioButtonMenuItem) {
                    ((JRadioButtonMenuItem) item).setSelected(false);
                }
            }
        }

        // Save the preference
        if (saveSettingsEnabled) {
            prefs.put("language", lang.name());
        }
        if (lang != Language.CUSTOM) {
            languageManager.loadLanguage(lang);
        }
        applyLanguage();
    }

    private void applyLanguage() {
        setTitle(languageManager.get("WINDOW_TITLE") + " v" + APP_VERSION);
        fileMenu.setText(languageManager.get("FILE_MENU"));
        saveMenuItem.setText(languageManager.get("SAVE_MENU"));
        saveAsImageMenuItem.setText(languageManager.get("SAVE_IMAGE_MENU"));
        loadMenuItem.setText(languageManager.get("LOAD_MENU"));
        saveSettingsMenuItem.setText(languageManager.get("SAVE_SETTINGS_MENU"));

        themeMenu.setText(languageManager.get("THEME_MENU"));
        lightMenuItem.setText(languageManager.get("LIGHT_MODE_MENU"));
        darkMenuItem.setText(languageManager.get("DARK_MODE_MENU"));
        customThemesMenu.setText(languageManager.get("CUSTOM_THEMES_MENU"));
        saveAsNewThemeMenuItem.setText(languageManager.get("SAVE_AS_NEW_THEME_MENU"));
        deleteThemeMenuItem.setText(languageManager.get("DELETE_THEME_MENU"));
        exportThemeMenuItem.setText(languageManager.get("EXPORT_THEME_MENU"));
        importThemeMenuItem.setText(languageManager.get("IMPORT_THEME_MENU"));
        languageMenu.setText(languageManager.get("LANGUAGE_MENU"));
        koreanMenuItem.setText(languageManager.get("KOREAN_MENU"));
        englishMenuItem.setText(languageManager.get("ENGLISH_MENU"));
        chineseMenuItem.setText(languageManager.get("CHINESE_MENU"));
        japaneseMenuItem.setText(languageManager.get("JAPANESE_MENU"));
        customLanguageMenu.setText(languageManager.get("CUSTOM_LANGUAGE_MENU"));
        createLanguagePackMenuItem.setText(languageManager.get("CREATE_LANG_PACK_MENU"));
        deleteLanguagePackMenuItem.setText(languageManager.get("DELETE_LANG_PACK_MENU"));
        exportLanguagePackMenuItem.setText(languageManager.get("EXPORT_LANG_PACK_MENU"));
        importLanguagePackMenuItem.setText(languageManager.get("IMPORT_LANG_PACK_MENU"));

        helpMenu.setText(languageManager.get("HELP_MENU"));
        aboutMenuItem.setText(languageManager.get("ABOUT_MENU"));
        inputLabel.setText(languageManager.get("INPUT_LABEL"));
        outputLabel.setText(languageManager.get("OUTPUT_LABEL"));
        transformButton.setText(languageManager.get("TRANSFORM_BUTTON"));
        clearButton.setText(languageManager.get("CLEAR_BUTTON"));
        copyButton.setText(languageManager.get("COPY_BUTTON"));
        // Clear feedback message on language change to avoid showing it in the wrong language
        if (feedbackLabel != null) {
            feedbackLabel.setText("");
        }

        cipherLabel.setText(languageManager.get("CIPHER_LABEL"));

        // To preserve selection, get the selected index before removing items
        int selectedIndex = cipherSelector.getSelectedIndex();
        if (selectedIndex == -1) selectedIndex = 0; // Default to first item

        cipherSelector.removeAllItems();
        cipherSelector.addItem(languageManager.get("CIPHER_ATBASH"));
        cipherSelector.addItem(languageManager.get("CIPHER_MORSE"));
        cipherSelector.addItem(languageManager.get("CIPHER_BINARY"));
        cipherSelector.addItem(languageManager.get("CIPHER_CAESAR_ENCRYPT"));
        cipherSelector.addItem(languageManager.get("CIPHER_CAESAR_DECRYPT"));
        cipherSelector.addItem(languageManager.get("CIPHER_SCYTALE_ENCRYPT"));
        cipherSelector.addItem(languageManager.get("CIPHER_SCYTALE_DECRYPT"));
        cipherSelector.addItem(languageManager.get("CIPHER_CHAINED"));
        cipherSelector.addItem(languageManager.get("CIPHER_CHAINED_DECRYPT"));

        cipherSelector.setSelectedIndex(selectedIndex);
        // Update parameter label language if it's visible

        updateParameterPanel(selectedIndex);
    }

    private void applyTheme(Theme theme) {
        // Set UIManager defaults to ensure all components (including popups) follow the theme.
        // This is especially important for Nimbus L&F.
        Color controlTextColor = (theme == Theme.DARK) ? Color.WHITE : theme.foreground;
        // 요청에 따라 다크 모드에서도 툴바 관련 항목은 라이트 모드의 텍스트 색상을 사용합니다.
        Color toolbarRelatedColor = (theme == Theme.DARK) ? Theme.LIGHT.foreground : theme.foreground;

        UIManager.put("PopupMenu.background", theme.menuBarBg);
        UIManager.put("MenuItem.background", theme.menuBarBg);
        UIManager.put("RadioButtonMenuItem.background", theme.menuBarBg);
        // 툴바와 그 하위 메뉴들의 텍스트 색상을 지정합니다.
        UIManager.put("Menu.foreground", toolbarRelatedColor);
        UIManager.put("MenuItem.foreground", toolbarRelatedColor);
        UIManager.put("RadioButtonMenuItem.foreground", toolbarRelatedColor);
        // Color for when a menu item is selected/highlighted
        UIManager.put("Menu.selectionBackground", theme.primaryButton);
        UIManager.put("MenuItem.selectionBackground", theme.primaryButton);
        UIManager.put("RadioButtonMenuItem.selectionBackground", theme.primaryButton);
        UIManager.put("Menu.selectionForeground", Color.WHITE);
        UIManager.put("MenuItem.selectionForeground", Color.WHITE);
        UIManager.put("RadioButtonMenuItem.selectionForeground", Color.WHITE);
        // JComboBox 팝업 리스트의 선택 색상을 메뉴와 통일합니다.

        // 렌더러가 super.getListCellRendererComponent를 호출할 때 이 값이 사용됩니다.
        UIManager.put("ComboBox.selectionBackground", theme.primaryButton);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);

        // Main Panel and Frame
        getContentPane().setBackground(theme.background);
        if (mainPanel != null) mainPanel.setBackground(theme.background);
        if (buttonPanel != null) buttonPanel.setBackground(theme.background);

        // Text Areas
        inputTextArea.setBackground(theme.textAreaBg);
        inputTextArea.setForeground(theme.foreground);
        inputTextArea.setCaretColor(theme.foreground);
        outputTextArea.setBackground(theme.outputTextAreaBg);
        outputTextArea.setForeground(theme.foreground);

        // Labels
        if (inputLabel != null) inputLabel.setForeground(theme.foreground);
        if (outputLabel != null) outputLabel.setForeground(theme.foreground);

        if (cipherLabel != null) cipherLabel.setForeground(controlTextColor);
        if (parameterLabel != null) parameterLabel.setForeground(toolbarRelatedColor);
        if (parameterLabel2 != null) parameterLabel2.setForeground(toolbarRelatedColor);
        if (feedbackLabel != null) feedbackLabel.setForeground(theme.primaryButton);

        // Icons
        if (saveIcon != null) saveIcon.setColor(toolbarRelatedColor);

        if (loadIcon != null) loadIcon.setColor(toolbarRelatedColor);
        if (saveImageIcon != null) saveImageIcon.setColor(toolbarRelatedColor);
        // Repaint menu bar to reflect icon color change
        if (menuBar != null) menuBar.repaint();

        // Buttons
        transformButton.setBackground(theme.primaryButton);
        clearButton.setBackground(theme.secondaryButton);
        copyButton.setBackground(theme.secondaryButton);

        // Menu Bar
        if (menuBar != null) {
            menuBar.setBackground(theme.menuBarBg);
            Border padding = BorderFactory.createEmptyBorder(4, 0, 4, 0);
            Border shadow = BorderFactory.createMatteBorder(0, 0, 1, 0, theme.menuBarBg.darker());
            menuBar.setBorder(BorderFactory.createCompoundBorder(padding, shadow));
        }

        // JComboBox
        if (cipherSelector != null) {
            cipherSelector.setBackground(theme.textAreaBg);
            cipherSelector.setForeground(toolbarRelatedColor);
            // Add a border that matches the theme for consistency
            cipherSelector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(3, 5, 3, 5) // Inner padding
            ));
        }

        // JSpinner
        configureSpinner(parameterSpinner, theme, toolbarRelatedColor);
        configureSpinner(parameterSpinner2, theme, toolbarRelatedColor);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void applyCustomColors(Color mainBg, Color textAreaBg, Color textColor, Color primaryBtn, Color secondaryBtn) {
        // Main Panel and Frame
        getContentPane().setBackground(mainBg);
        if (mainPanel != null) mainPanel.setBackground(mainBg);
        if (buttonPanel != null) buttonPanel.setBackground(mainBg);
        if (parameterPanel != null) parameterPanel.setOpaque(false); // Ensure it's transparent

        // Text Areas
        inputTextArea.setBackground(textAreaBg);
        inputTextArea.setForeground(textColor);
        inputTextArea.setCaretColor(textColor);
        // Make output area slightly darker for visual distinction
        outputTextArea.setBackground(new Color(
            Math.max(0, textAreaBg.getRed() - 10),
            Math.max(0, textAreaBg.getGreen() - 10),
            Math.max(0, textAreaBg.getBlue() - 10)
        ));
        outputTextArea.setForeground(textColor);

        // Labels
        if (inputLabel != null) inputLabel.setForeground(textColor);
        if (outputLabel != null) outputLabel.setForeground(textColor);
        if (cipherLabel != null) cipherLabel.setForeground(textColor);
        if (parameterLabel != null) parameterLabel.setForeground(textColor);
        if (parameterLabel2 != null) parameterLabel2.setForeground(textColor);
        if (feedbackLabel != null) feedbackLabel.setForeground(primaryBtn);

        // Buttons
        transformButton.setBackground(primaryBtn);
        clearButton.setBackground(secondaryBtn);
        copyButton.setBackground(secondaryBtn);

        Theme tempTheme = new Theme(mainBg, textAreaBg, outputTextArea.getBackground(), textColor, primaryBtn, secondaryBtn, mainBg.darker(), menuBar.getBackground(), "Custom");

        if (cipherSelector != null) {
            cipherSelector.setBackground(tempTheme.textAreaBg);
            cipherSelector.setForeground(textColor);
            cipherSelector.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tempTheme.border),
                BorderFactory.createEmptyBorder(3, 5, 3, 5)
            ));
        }

        configureSpinner(parameterSpinner, tempTheme, textColor);
        configureSpinner(parameterSpinner2, tempTheme, textColor);

        SwingUtilities.updateComponentTreeUI(this);
    }

    private void applyCustomLanguage(String langName) {
        languageManager.loadCustomLanguage(prefs, langName);
        setLanguage(Language.CUSTOM); // This updates UI text via applyLanguage()

        if (saveSettingsEnabled) {
            prefs.put("language", Language.CUSTOM.name());
            prefs.put("customLanguageName", langName);
        }
    }

    private void selectCustomLanguageMenuItem(String langName) {
        for (Component item : customLanguageMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem) {
                JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) item;
                if (langName.equals(menuItem.getName())) {
                    menuItem.setSelected(true);
                    break;
                }
            }
        }
    }

    private void applyCustomTheme(String themeName) {
        try {
            // Load custom theme colors from preferences for the given name
            Color mainBg = new Color(prefs.getInt("custom.theme." + themeName + ".main.background", Theme.LIGHT.background.getRGB()));
            Color textAreaBg = new Color(prefs.getInt("custom.theme." + themeName + ".textarea.background", Theme.LIGHT.textAreaBg.getRGB()));
            Color textColor = new Color(prefs.getInt("custom.theme." + themeName + ".text.foreground", Theme.LIGHT.foreground.getRGB()));
            Color primaryBtn = new Color(prefs.getInt("custom.theme." + themeName + ".button.primary", Theme.LIGHT.primaryButton.getRGB()));
            Color secondaryBtn = new Color(prefs.getInt("custom.theme." + themeName + ".button.secondary", Theme.LIGHT.secondaryButton.getRGB()));

            applyCustomColors(mainBg, textAreaBg, textColor, primaryBtn, secondaryBtn);

            // Save this as the last used theme
            if (saveSettingsEnabled) {
                prefs.put("theme", themeName);
            }
        } catch (Exception e) {
            // If loading fails, fall back to default Light theme
            setTheme(ThemeMode.LIGHT);
        }
    }

    private void configureSpinner(JSpinner spinner, Theme theme, Color controlTextColor) {
        if (spinner == null) return;
        // Remove the border from the spinner container itself to avoid double borders
        spinner.setBorder(BorderFactory.createEmptyBorder());
        JComponent editor = spinner.getEditor();
        // Style the editor component which contains the text field
        editor.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(2, 6, 2, 6) // Inner padding
        ));
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setForeground(controlTextColor);
            tf.setBackground(theme.textAreaBg);
            tf.setCaretColor(theme.foreground);
            // The text field's own border should be removed as the editor now has one.
            tf.setBorder(null);
        }
    }

    private void configureButton(JButton button, Font font) {
        button.setFont(font);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        // Set internal padding to make the button larger
        button.setMargin(new Insets(8, 20, 8, 20));
    }

    private void initComponents() {
        // Define a modern font
        Font baseFont = new Font("SansSerif", Font.PLAIN, 14);
 
        // Input Text Area
        inputTextArea = new JTextArea();
        inputTextArea.setFont(baseFont);
        inputTextArea.setLineWrap(true);
        inputTextArea.setWrapStyleWord(true);
        inputTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // Inner padding
        inputTextArea.setOpaque(false); // Crucial for rounded corners to show through
 
        // Output Text Area
        outputTextArea = new JTextArea();
        outputTextArea.setFont(baseFont);
        outputTextArea.setLineWrap(true);
        outputTextArea.setWrapStyleWord(true);
        outputTextArea.setEditable(false);
        outputTextArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // Inner padding
        outputTextArea.setOpaque(false); // Crucial for rounded corners to show through
 
        // Buttons
        transformButton = new RoundedButton("", 15);
        clearButton = new RoundedButton("", 15);
        copyButton = new RoundedButton("", 15);

        // Configure button styles for a better look and feel
        configureButton(transformButton, baseFont.deriveFont(Font.BOLD));
        configureButton(clearButton, baseFont);
        configureButton(copyButton, baseFont);

        // Feedback Label
        feedbackLabel = new JLabel();
        feedbackLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        // Icons
        saveIcon = new MenuIcon(MenuIcon.IconType.SAVE);
        loadIcon = new MenuIcon(MenuIcon.IconType.LOAD);
        saveImageIcon = new MenuIcon(MenuIcon.IconType.SAVE_IMAGE);

        // Cipher Selector
        cipherSelector = new JComboBox<>();
        // JComboBox 팝업 리스트의 렌더러를 설정하여, 다크 모드에서도 팝업이 라이트 모드처럼 보이게 합니다.
        cipherSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                // Always render the popup list with a light theme for readability
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                c.setBackground(Color.WHITE); // Force white background for all items
                c.setForeground(Theme.LIGHT.foreground); // Force dark text for all items

                // Override selection colors
                if (isSelected) {
                    // Use the UIManager properties set in applyTheme
                    c.setBackground(list.getSelectionBackground());
                    c.setForeground(list.getSelectionForeground());
                }
                // 선택된 항목의 색상은 applyTheme에서 UIManager를 통해 설정되므로, super() 호출로 충분합니다.
                return this;
            }
        });

        // Parameter Panel
        parameterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        parameterPanel.setOpaque(false);
        parameterLabel = new JLabel();
        parameterSpinner = new JSpinner();
        parameterLabel2 = new JLabel();
        parameterSpinner2 = new JSpinner();
        parameterPanel.add(parameterLabel);
        parameterPanel.add(parameterSpinner);
        parameterPanel.add(parameterLabel2);
        parameterPanel.add(parameterSpinner2);
        parameterPanel.setVisible(false); // Initially hidden
    }

    private void initLayout() {
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Outer padding
        GridBagConstraints gbc = new GridBagConstraints();
 
        // --- Row 0: Input Label ---
        inputLabel = new JLabel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 5, 5, 0);
        mainPanel.add(inputLabel, gbc);
 
        // --- Row 1: Input TextArea ---
        inputScrollPane = new RoundedScrollPane(inputTextArea, 15);
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 10, 0);
        mainPanel.add(inputScrollPane, gbc);
 
        // --- Row 2: Output Label ---
        outputLabel = new JLabel();
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(0, 5, 5, 0);
        mainPanel.add(outputLabel, gbc);
 
        // --- Row 3: Output TextArea ---
        outputScrollPane = new RoundedScrollPane(outputTextArea, 15);
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 15, 0);
        mainPanel.add(outputScrollPane, gbc);
 
        // --- Row 4: Bottom Panel (Feedback + Buttons) ---
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setOpaque(false); // Inherit background from mainPanel
        GridBagConstraints gbcBottom = new GridBagConstraints();

        // West part: Cipher selection
        JPanel cipherPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        cipherPanel.setOpaque(false);
        cipherLabel = new JLabel();
        cipherPanel.add(cipherLabel);
        cipherPanel.add(cipherSelector);
        cipherPanel.add(parameterPanel);
        gbcBottom.gridx = 0;
        gbcBottom.gridy = 0;
        gbcBottom.weightx = 0; // Do not allow this panel to grow horizontally on its own.
        gbcBottom.anchor = GridBagConstraints.WEST;
        bottomPanel.add(cipherPanel, gbcBottom);

        // Center feedback label, which will act as a flexible spacer
        feedbackLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbcBottom.gridx = 1;
        gbcBottom.weightx = 1.0; // Allow this to take up all extra horizontal space.
        gbcBottom.fill = GridBagConstraints.HORIZONTAL;
        bottomPanel.add(feedbackLabel, gbcBottom);

        // East part: Buttons
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false); // Inherit background from mainPanel
        buttonPanel.add(copyButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(transformButton);
        gbcBottom.gridx = 2;
        gbcBottom.weightx = 0; // Do not allow this to grow.
        gbcBottom.fill = GridBagConstraints.NONE;
        gbcBottom.anchor = GridBagConstraints.EAST;
        bottomPanel.add(buttonPanel, gbcBottom);
 
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(bottomPanel, gbc);
 
        // Add the fully constructed main panel to the frame's content pane.
        add(mainPanel);
    }
    private void initListeners() {
        // Add a window listener to save settings on close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveWindowGeometry();
            }
        });

        // Listener for the "Save Settings" checkbox. This one *always* saves its state.
        saveSettingsMenuItem.addActionListener(e -> {
            saveSettingsEnabled = saveSettingsMenuItem.isSelected();
            prefs.putBoolean("saveSettingsEnabled", saveSettingsEnabled);
        });

        // Define and add the listener to save cipher selection when it changes
        cipherSaveListener = e -> {
            int selectedIndex = cipherSelector.getSelectedIndex();
            if (selectedIndex == -1) return;

            if (saveSettingsEnabled) {
                prefs.putInt("cipherMode", selectedIndex);
            }
            updateParameterPanel(selectedIndex);
        };
        cipherSelector.addActionListener(cipherSaveListener);

        // Add listener to save spinner values when they change
        parameterSpinner.addChangeListener(e -> saveSpinnerValue());
        parameterSpinner2.addChangeListener(e -> saveSpinnerValue());

       transformButton.addActionListener(e -> {
            // Disable the button to prevent multiple clicks
            transformButton.setEnabled(false);
            outputTextArea.setText(""); // Clear previous result

            // Use a SwingWorker to perform the transformation off the EDT
            CipherAnimationWorker worker = new CipherAnimationWorker(inputTextArea.getText()) {
                @Override
                protected void done() {
                    try {
                        String result = get(); // Get the result from doInBackground
                        startAnimation(result); // Start the animation on the EDT
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        // Handle exceptions, e.g., show an error message
                        outputTextArea.setText("Error: " + ex.getMessage());
                    } finally {
                        // Re-enable the button
                        transformButton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        });
 
        clearButton.addActionListener(e -> {
            inputTextArea.setText("");
            outputTextArea.setText("");
        });

       copyButton.addActionListener(e -> {
            String textToCopy = outputTextArea.getText();
            if (textToCopy != null && !textToCopy.isEmpty()) {
                StringSelection stringSelection = new StringSelection(textToCopy);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(stringSelection, null);

                // Show feedback message with a fade-out animation
                animateFeedbackLabel();
            } else {
                // Easter egg: Show "LOL" when trying to copy nothing.
                outputTextArea.setText("LOL");
            }
        });

        saveAsImageMenuItem.addActionListener(e -> saveOutputAsImage());

        saveMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(languageManager.get("SAVE_MENU"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                // Ensure the file has a .txt extension
                if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                    fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".txt");
                }

                try (FileWriter writer = new FileWriter(fileToSave)) {
                    writer.write(outputTextArea.getText());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            languageManager.get("SAVE_ERROR_MESSAGE") + ex.getMessage(),
                            languageManager.get("SAVE_ERROR_TITLE"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });




        loadMenuItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle(languageManager.get("LOAD_MENU"));
            fileChooser.setFileFilter(new FileNameExtensionFilter("Text Files (*.txt)", "txt"));

            int userSelection = fileChooser.showOpenDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToLoad = fileChooser.getSelectedFile();
                try {
                    String content = Files.readString(fileToLoad.toPath());
                    inputTextArea.setText(content);
                    // Clear the output area when loading new text
                    outputTextArea.setText("");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this,
                            languageManager.get("LOAD_ERROR_MESSAGE") + ex.getMessage(),
                            languageManager.get("LOAD_ERROR_TITLE"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        aboutMenuItem.addActionListener(e -> showHelpDialog());

        saveAsNewThemeMenuItem.addActionListener(e -> promptAndSaveNewTheme());
        deleteThemeMenuItem.addActionListener(e -> deleteSelectedCustomTheme());
        exportThemeMenuItem.addActionListener(e -> exportCurrentTheme());
        importThemeMenuItem.addActionListener(e -> importTheme());

        createLanguagePackMenuItem.addActionListener(e -> createNewLanguagePack());
        deleteLanguagePackMenuItem.addActionListener(e -> deleteSelectedLanguagePack());
        exportLanguagePackMenuItem.addActionListener(e -> exportLanguagePack());
        importLanguagePackMenuItem.addActionListener(e -> importLanguagePack());
    }

    private void animateFeedbackLabel() {
        // Stop any existing timer to reset the animation
        if (feedbackTimer != null && feedbackTimer.isRunning()) {
            feedbackTimer.stop();
        }

        feedbackLabel.setText(languageManager.get("COPY_FEEDBACK"));
        final Color originalColor = feedbackLabel.getForeground();
        // Ensure the label is fully opaque at the start
        feedbackLabel.setForeground(new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), 255));

        // Timer to fade out the label
        final long startTime = System.currentTimeMillis();
        final int displayDuration = 1000; // Time to stay fully visible
        final int fadeDuration = 1000;    // Time to fade out
        final int totalDuration = displayDuration + fadeDuration;

        feedbackTimer = new Timer(20, e -> { // Update every 20ms for smooth animation
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed >= totalDuration) {
                feedbackLabel.setText("");
                feedbackLabel.setForeground(originalColor); // Reset for next time
                ((Timer) e.getSource()).stop();
                return;
            }

            // Start fading after the initial display duration
            if (elapsed > displayDuration) {
                float progress = (float) (elapsed - displayDuration) / fadeDuration;
                int alpha = (int) (255 * (1 - progress));
                alpha = Math.max(0, Math.min(255, alpha));
                feedbackLabel.setForeground(new Color(originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), alpha));
            }
        });
        feedbackTimer.setRepeats(true);
        feedbackTimer.start();
    }

    private void showHelpDialog() {
        try (InputStream is = getClass().getResourceAsStream("README.txt")) {
            if (is == null) {
                JOptionPane.showMessageDialog(this, "README.txt file not found in application resources.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            // Use a JTextArea inside a JScrollPane to show the content
            JTextArea textArea = new JTextArea(content);
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false);
            // Let the Look & Feel manage the colors for consistency within the dialog

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 150));
            JOptionPane.showMessageDialog(this, scrollPane, languageManager.get("HELP_TITLE"), JOptionPane.INFORMATION_MESSAGE);

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error reading help file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String performTransformation(String inputText, int selectedCipherIndex, int key, int key2) {
        String transformedText = "";
        switch (selectedCipherIndex) {
            case 0: // 아트배쉬
                transformedText = AtbashCipher.transform(inputText);
                break;
            case 1: // 모스 부호 (자동 감지)
                if (MorseCode.isMorseCode(inputText)) {
                    transformedText = MorseCode.fromMorse(inputText); // 모스 부호 -> 텍스트
                } else {
                    transformedText = MorseCode.toMorse(inputText); // 텍스트 -> 모스 부호
                }
                break;
            case 2: // 이진수 (자동 감지)
                if (BinaryCipher.isBinary(inputText)) {
                    transformedText = BinaryCipher.fromBinary(inputText);
                } else {
                    transformedText = BinaryCipher.toBinary(inputText);
                }
                break;
            case 3: // 카이사르 (암호화)
                transformedText = CaesarCipher.encrypt(inputText, key);
                break;
            case 4: // 카이사르 (복호화)
                transformedText = CaesarCipher.decrypt(inputText, key);
                break;
            case 5: // 스퀴탈레 (암호화)
                transformedText = ScytaleCipher.encrypt(inputText, key);
                break;
            case 6: // 스퀴탈레 (복호화)
                transformedText = ScytaleCipher.decrypt(inputText, key);
                break;
            case 7: // Chained
                String atbashResult = AtbashCipher.transform(inputText);
                String scytaleResult = ScytaleCipher.encrypt(atbashResult, key); // key is diameter
                String caesarResult = CaesarCipher.encrypt(scytaleResult, key2); // key2 is shift
                transformedText = MorseCode.toMorse(caesarResult);
                break;
            case 8: // Chained Decrypt
                String morseResult = MorseCode.fromMorse(inputText);
                String caesarDecryptResult = CaesarCipher.decrypt(morseResult, key2); // key2 is shift
                String scytaleDecryptResult = ScytaleCipher.decrypt(caesarDecryptResult, key); // key is diameter
                transformedText = AtbashCipher.transform(scytaleDecryptResult); // Atbash is its own inverse
                break;
        }
        return transformedText;
    }

    /**
     * A SwingWorker to perform cipher transformation in the background
     * and animate the result in the output text area.
     */
    private class CipherAnimationWorker extends SwingWorker<String, Void> {
        private final String inputText;

        public CipherAnimationWorker(String inputText) {
            this.inputText = inputText;
        }

        @Override
        protected String doInBackground() throws Exception {
            // This runs on a background thread.
            int selectedCipherIndex = cipherSelector.getSelectedIndex();
            int key = 0;
            int key2 = 0;

            // We need to get spinner values on the EDT, but it's generally safe here
            // as the UI is disabled. For robustness, one might pass these values
            // into the worker's constructor.
            if (parameterPanel.isVisible()) {
                key = (Integer) parameterSpinner.getValue();
                if (parameterSpinner2.isVisible()) {
                    key2 = (Integer) parameterSpinner2.getValue();
                }
            }

            return performTransformation(inputText, selectedCipherIndex, key, key2);
        }

        public void startAnimation(String resultText) {
            // This method is called from done() on the EDT.
            animateText(outputTextArea, resultText);
        }
    }

    private void animateText(JTextArea textArea, String text) {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }
        final int[] charIndex = {0};
        textArea.setText("");

        animationTimer = new Timer(15, e -> { // Adjust delay for speed
            if (charIndex[0] < text.length()) {
                textArea.append(String.valueOf(text.charAt(charIndex[0])));
                charIndex[0]++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        animationTimer.start();
    }

    private void exportCurrentTheme() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.get("EXPORT_THEME_DIALOG_TITLE"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("ArtBash Theme (*.theme)", "theme"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".theme")) {
                file = new File(file.getParentFile(), file.getName() + ".theme");
            }

            Properties themeProps = new Properties();
            themeProps.setProperty("main.background", String.valueOf(mainPanel.getBackground().getRGB()));
            themeProps.setProperty("textarea.background", String.valueOf(inputTextArea.getBackground().getRGB()));
            themeProps.setProperty("text.foreground", String.valueOf(inputTextArea.getForeground().getRGB()));
            themeProps.setProperty("button.primary", String.valueOf(transformButton.getBackground().getRGB()));
            themeProps.setProperty("button.secondary", String.valueOf(clearButton.getBackground().getRGB()));

            try (OutputStream out = new FileOutputStream(file)) {
                themeProps.store(out, "Custom ArtBash Theme");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                        languageManager.get("EXPORT_THEME_ERROR_MESSAGE") + ex.getMessage(),
                        languageManager.get("EXPORT_THEME_ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importTheme() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.get("IMPORT_THEME_DIALOG_TITLE"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("ArtBash Theme (*.theme)", "theme"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Properties themeProps = new Properties();

            try (InputStream in = new FileInputStream(file)) {
                themeProps.load(in);

                String themeName = JOptionPane.showInputDialog(this,
                        languageManager.get("ENTER_THEME_NAME_MESSAGE"),
                        languageManager.get("ENTER_THEME_NAME_TITLE"),
                        JOptionPane.PLAIN_MESSAGE);

                if (themeName != null && !themeName.isBlank()) {
                    themeName = themeName.trim();
                    if (themeName.equalsIgnoreCase("LIGHT") || themeName.equalsIgnoreCase("DARK") || themeName.equalsIgnoreCase("CUSTOM")) {
                        JOptionPane.showMessageDialog(this, languageManager.get("INVALID_THEME_NAME_MESSAGE"), languageManager.get("INVALID_THEME_NAME_TITLE"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Save the imported theme's colors under the new name
                    prefs.putInt("custom.theme." + themeName + ".main.background", Integer.parseInt(themeProps.getProperty("main.background")));
                    prefs.putInt("custom.theme." + themeName + ".textarea.background", Integer.parseInt(themeProps.getProperty("textarea.background")));
                    prefs.putInt("custom.theme." + themeName + ".text.foreground", Integer.parseInt(themeProps.getProperty("text.foreground")));
                    prefs.putInt("custom.theme." + themeName + ".button.primary", Integer.parseInt(themeProps.getProperty("button.primary")));
                    prefs.putInt("custom.theme." + themeName + ".button.secondary", Integer.parseInt(themeProps.getProperty("button.secondary")));

                    // Update the list of theme names
                    List<String> names = getCustomThemeNames();
                    if (!names.contains(themeName)) {
                        names.add(themeName);
                        saveCustomThemeNames(names);
                        rebuildCustomThemesMenu(names);
                    }

                    // Apply and select the new theme
                    applyCustomTheme(themeName);
                    for (Component item : customThemesMenu.getMenuComponents()) {
                        if (item instanceof JRadioButtonMenuItem && themeName.equals(item.getName())) {
                            ((JRadioButtonMenuItem) item).setSelected(true);
                            break;
                        }
                    }
                } else if (themeName != null) {
                    JOptionPane.showMessageDialog(this, languageManager.get("INVALID_THEME_NAME_MESSAGE"), languageManager.get("INVALID_THEME_NAME_TITLE"), JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException | NumberFormatException | NullPointerException ex) {
                ex.printStackTrace(); // For debugging
                JOptionPane.showMessageDialog(this,
                        languageManager.get("IMPORT_THEME_ERROR_MESSAGE"),
                        languageManager.get("IMPORT_THEME_ERROR_TITLE"),
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedCustomTheme() {
        String selectedThemeName = null;
        for (Component item : customThemesMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) item).isSelected()) {
                selectedThemeName = item.getName();
                break;
            }
        }

        if (selectedThemeName == null) {
            JOptionPane.showMessageDialog(this, languageManager.get("NO_CUSTOM_THEME_SELECTED_MESSAGE"), languageManager.get("NO_CUSTOM_THEME_SELECTED_TITLE"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = languageManager.get("DELETE_THEME_CONFIRM_MESSAGE").replace("{0}", selectedThemeName);
        int result = JOptionPane.showConfirmDialog(this, message, languageManager.get("DELETE_THEME_CONFIRM_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            removeCustomTheme(selectedThemeName);
            List<String> names = getCustomThemeNames();
            names.remove(selectedThemeName);
            saveCustomThemeNames(names);
            rebuildCustomThemesMenu(names);
            setTheme(ThemeMode.LIGHT); // Revert to default theme
        }
    }

    private void createNewLanguagePack() {
        // 1. Get a base set of keys from the English pack
        TreeMap<String, String> currentStrings = languageManager.getAllStrings();
        if (currentStrings.isEmpty()) {
            // Fallback to loading English if manager is empty
            languageManager.loadLanguage(Language.ENGLISH);
            currentStrings = languageManager.getAllStrings();
        }

        // 2. Create a table model and table for editing
        String[] columnNames = {"Key", "Translation"};
        Object[][] data = new Object[currentStrings.size()][2];
        int i = 0;
        for (var entry : currentStrings.entrySet()) {
            data[i][0] = entry.getKey();
            data[i][1] = entry.getValue();
            i++;
        }

        JTable table = new JTable(data, columnNames);
        table.getColumnModel().getColumn(0).setMinWidth(150);
        table.getColumnModel().getColumn(0).setMaxWidth(200);
        table.getColumnModel().getColumn(1).setMinWidth(300);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        // 3. Show the dialog
        int result = JOptionPane.showConfirmDialog(this, scrollPane, languageManager.get("CREATE_LANG_PACK_TITLE"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            // 4. Prompt for a name
            String langName = JOptionPane.showInputDialog(this, languageManager.get("ENTER_LANG_PACK_NAME_MESSAGE"), languageManager.get("ENTER_LANG_PACK_NAME_TITLE"), JOptionPane.PLAIN_MESSAGE);

            if (langName != null && !langName.isBlank()) {
                langName = langName.trim();

                // 5. Save the new language pack to preferences
                for (int row = 0; row < table.getRowCount(); row++) {
                    String key = (String) table.getValueAt(row, 0);
                    String value = (String) table.getValueAt(row, 1);
                    prefs.put("custom.lang." + langName + "." + key, value);
                }

                // 6. Update the list of language pack names
                List<String> names = getCustomLanguagePackNames();
                if (!names.contains(langName)) {
                    names.add(langName);
                    saveCustomLanguagePackNames(names);
                    rebuildCustomLanguageMenu(names);
                }

                // 7. Apply and select the new language pack
                applyCustomLanguage(langName);
                selectCustomLanguageMenuItem(langName);

            } else if (langName != null) {
                JOptionPane.showMessageDialog(this, languageManager.get("INVALID_LANG_PACK_NAME_MESSAGE"), languageManager.get("INVALID_LANG_PACK_NAME_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedLanguagePack() {
        String selectedLangName = null;
        for (Component item : customLanguageMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) item).isSelected()) {
                selectedLangName = item.getName();
                break;
            }
        }

        if (selectedLangName == null) {
            JOptionPane.showMessageDialog(this, languageManager.get("NO_LANG_PACK_SELECTED_MESSAGE"), languageManager.get("NO_LANG_PACK_SELECTED_TITLE"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        String message = languageManager.get("DELETE_LANG_PACK_CONFIRM_MESSAGE").replace("{0}", selectedLangName);
        int result = JOptionPane.showConfirmDialog(this, message, languageManager.get("DELETE_LANG_PACK_CONFIRM_TITLE"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            removeCustomLanguagePack(selectedLangName);
            List<String> names = getCustomLanguagePackNames();
            names.remove(selectedLangName);
            saveCustomLanguagePackNames(names);
            rebuildCustomLanguageMenu(names);
            setLanguage(Language.ENGLISH); // Revert to default language
        }
    }

    private void exportLanguagePack() {
        String selectedLangName = null;
        for (Component item : customLanguageMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) item).isSelected()) {
                selectedLangName = item.getName();
                break;
            }
        }

        if (selectedLangName == null) {
            JOptionPane.showMessageDialog(this, languageManager.get("NO_LANG_PACK_SELECTED_MESSAGE"), languageManager.get("NO_LANG_PACK_SELECTED_TITLE"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.get("EXPORT_LANG_PACK_DIALOG_TITLE"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Language Pack (*.json)", "json"));
        fileChooser.setSelectedFile(new File(selectedLangName + ".json"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (Writer writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                writer.write("{\n");
                TreeMap<String, String> strings = languageManager.getAllStrings();
                int count = 0;
                for (var entry : strings.entrySet()) {
                    writer.write(String.format("  \"%s\": \"%s\"", entry.getKey(), entry.getValue().replace("\"", "\\\"")));
                    if (++count < strings.size()) {
                        writer.write(",\n");
                    } else {
                        writer.write("\n");
                    }
                }
                writer.write("}\n");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, languageManager.get("EXPORT_LANG_PACK_ERROR_MESSAGE") + ex.getMessage(), languageManager.get("EXPORT_LANG_PACK_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importLanguagePack() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.get("IMPORT_LANG_PACK_DIALOG_TITLE"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("JSON Language Pack (*.json)", "json"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String langName = file.getName().replaceFirst("[.][^.]+$", ""); // Get name from filename

            try (InputStream is = new FileInputStream(file)) {
                languageManager.loadLanguageFromStream(is);
                TreeMap<String, String> importedStrings = languageManager.getAllStrings();

                // Save the imported language pack to preferences
                for (var entry : importedStrings.entrySet()) {
                    prefs.put("custom.lang." + langName + "." + entry.getKey(), entry.getValue());
                }

                // Update the list of language pack names
                List<String> names = getCustomLanguagePackNames();
                if (!names.contains(langName)) {
                    names.add(langName);
                    saveCustomLanguagePackNames(names);
                    rebuildCustomLanguageMenu(names);
                }

                // Apply and select the new language pack
                applyCustomLanguage(langName);
                selectCustomLanguageMenuItem(langName);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, languageManager.get("IMPORT_LANG_PACK_ERROR_MESSAGE"), languageManager.get("IMPORT_LANG_PACK_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSpinnerValue() {
        if (!saveSettingsEnabled) return;

        int selectedIndex = cipherSelector.getSelectedIndex();
        Object value = parameterSpinner.getValue();
        if (value instanceof Integer) {
            if (selectedIndex >= 3 && selectedIndex <= 4) { // Caesar
                prefs.putInt("caesarShift", (Integer) value);
            } else if (selectedIndex >= 5 && selectedIndex <= 6) { // Scytale
                prefs.putInt("scytaleDiameter", (Integer) value);
            } else if (selectedIndex >= 7 && selectedIndex <= 8) { // Chained Encrypt/Decrypt
                // Spinner 1 is Scytale diameter
                prefs.putInt("scytaleDiameter", (Integer) value);
                // Spinner 2 is Caesar shift
                Object value2 = parameterSpinner2.getValue();
                if (value2 instanceof Integer) {
                    prefs.putInt("caesarShift", (Integer) value2);
                }
            }
        }
    }

    private void saveWindowGeometry() {
        if (!saveSettingsEnabled) return;

        Rectangle bounds = getBounds();
        prefs.putInt("windowWidth", bounds.width);
        prefs.putInt("windowHeight", bounds.height);
    }

    private List<String> getCustomThemeNames() {
        String names = prefs.get("custom.themes.list", "");
        if (names.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(names.split(",")));
    }

    private void saveCustomThemeNames(List<String> names) {
        prefs.put("custom.themes.list", String.join(",", names));
    }

    private List<String> getCustomLanguagePackNames() {
        String names = prefs.get("custom.languages.list", "");
        if (names.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(List.of(names.split(",")));
    }

    private void saveCustomLanguagePackNames(List<String> names) {
        prefs.put("custom.languages.list", String.join(",", names));
    }

    private void rebuildCustomLanguageMenu(List<String> langNames) {
        // Remove only custom language items from the group
        for (Component item : customLanguageMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem) {
                languageGroup.remove((AbstractButton) item);
            }
        }
        customLanguageMenu.removeAll();

        for (String name : langNames) {
            JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(name);
            langItem.setName(name);
            langItem.addActionListener(e -> applyCustomLanguage(name));
            languageGroup.add(langItem);
            customLanguageMenu.add(langItem);
        }
        customLanguageMenu.setEnabled(!langNames.isEmpty());
    }

    private void removeCustomLanguagePack(String langName) {
        try { prefs.node("custom.lang." + langName).removeNode(); } catch (Exception e) { /* Ignored */ }
    }

    private void rebuildCustomThemesMenu(List<String> themeNames) {
        // Remove only custom theme items from the group
        for (Component item : customThemesMenu.getMenuComponents()) {
            if (item instanceof JRadioButtonMenuItem) {
                themeGroup.remove((AbstractButton) item);
            }
        }
        customThemesMenu.removeAll();

        for (String name : themeNames) {
            JRadioButtonMenuItem themeItem = new JRadioButtonMenuItem(name);
            themeItem.setName(name); // Store name for later retrieval
            themeItem.addActionListener(e -> applyCustomTheme(name));
            themeGroup.add(themeItem);
            customThemesMenu.add(themeItem);
        }
        customThemesMenu.setEnabled(!themeNames.isEmpty());
    }

    private void removeCustomTheme(String themeName) {
        try { prefs.node("custom.theme." + themeName).removeNode(); } catch (Exception e) { /* Ignored */ }
    }

    private void saveOutputAsImage() {
        String text = outputTextArea.getText();
        if (text == null || text.isBlank()) {
            JOptionPane.showMessageDialog(this,
                    languageManager.get("SAVE_IMAGE_NO_CONTENT_MESSAGE"),
                    languageManager.get("SAVE_IMAGE_NO_CONTENT_TITLE"),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(languageManager.get("SAVE_IMAGE_MENU"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images (*.png)", "png"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".png")) {
                fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".png");
            }

            try {
                // 1. Get text and font details
                Font font = outputTextArea.getFont();
                FontMetrics metrics = getFontMetrics(font);
                String[] lines = text.split("\n");

                // 2. Calculate image dimensions
                int padding = 20;
                int lineHeight = metrics.getHeight();
                int imageWidth = 0;
                for (String line : lines) {
                    imageWidth = Math.max(imageWidth, metrics.stringWidth(line));
                }
                imageWidth += padding * 2;
                int imageHeight = (lineHeight * lines.length) + padding * 2;

                // 3. Create image and graphics context
                BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = image.createGraphics();

                // 4. Draw on the image
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                // Use the actual colors from the text area to support custom themes
                g2d.setColor(outputTextArea.getBackground());
                g2d.fillRect(0, 0, imageWidth, imageHeight);

                g2d.setColor(outputTextArea.getForeground());
                g2d.setFont(font);

                int y = padding + metrics.getAscent();
                for (String line : lines) {
                    g2d.drawString(line, padding, y);
                    y += lineHeight;
                }

                g2d.dispose();

                // 5. Save the image
                ImageIO.write(image, "png", fileToSave);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, languageManager.get("SAVE_IMAGE_ERROR_MESSAGE") + ex.getMessage(), languageManager.get("SAVE_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Main method to run the GUI application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set a stable Look and Feel like Nimbus. System L&F can be unstable on some systems.
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // If Nimbus is not available, fall back to the default Metal L&F.
                e.printStackTrace();
            }

            // 1. Create and show the splash screen on the EDT.
            final Object[] splashAndBar = createSplashScreen();
            JWindow splash = (JWindow) splashAndBar[0];
            JProgressBar progressBar = (JProgressBar) splashAndBar[1];

            if (splash != null) {
                splash.setVisible(true);

                // Use a SwingWorker to handle the "loading" process (a simple sleep)
                // and update the progress bar, then launch the main app.
                SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        for (int i = 0; i <= 100; i++) {
                            Thread.sleep(10); // 10ms * 100 = 1 second total
                            publish(i);
                        }
                        return null;
                    }

                    @Override
                    protected void process(List<Integer> chunks) {
                        if (progressBar != null) {
                            progressBar.setValue(chunks.get(chunks.size() - 1));
                        }
                    }

                    @Override
                    protected void done() {
                        splash.dispose();
                        // 2. Once the splash is done, create and show the main application.
                        new AtbashCipherGUI().setVisible(true);
                    }
                };
                worker.execute();
            } else {
                // If splash screen fails, just launch the main app directly.
                new AtbashCipherGUI().setVisible(true);
            }
        });
    }

    /**
     * Creates a splash screen in a separate JWindow.
     * @return An array containing the JWindow and JProgressBar, or {null, null} if creation fails.
     */
    private static Object[] createSplashScreen() {
        URL imageURL = AtbashCipherGUI.class.getResource("startup.png");
        if (imageURL == null) {
            System.err.println("Warning: Splash screen image 'startup.png' not found.");
            return new Object[]{null, null};
        }
        ImageIcon icon = new ImageIcon(imageURL);

        // Create the splash window
        JWindow window = new JWindow();
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JLabel(icon), BorderLayout.CENTER);

        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setForeground(new Color(0, 123, 255));
        progressBar.setBackground(new Color(230, 230, 230));
        progressBar.setBorderPainted(false);
        contentPane.add(progressBar, BorderLayout.SOUTH);
        window.setContentPane(contentPane);
        window.pack();
        window.setLocationRelativeTo(null); // Center on screen
        // Return both the window and the progress bar for direct access
        return new Object[]{window, progressBar};
    }
}