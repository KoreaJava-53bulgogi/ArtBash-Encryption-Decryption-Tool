import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
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

    private enum Theme {
        LIGHT(
                new Color(245, 247, 249), // background
                Color.WHITE,              // textAreaBg
                new Color(235, 237, 239), // outputTextAreaBg
                new Color(50, 50, 50), // foreground
                new Color(0, 123, 255),   // primaryButton
                new Color(108, 117, 125), // secondaryButton
                new Color(220, 223, 226), // border
                Color.WHITE,               // menuBarBg
                "Light Theme"              // Theme Name
        ),
        DARK(
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

        final Color background;
        final Color textAreaBg;
        final Color outputTextAreaBg;
        final Color foreground;
        final Color primaryButton;
        final Color secondaryButton;
        final Color border;
        final Color menuBarBg;

        final String themeName;

        Theme(Color background, Color textAreaBg, Color outputTextAreaBg, Color foreground, Color primaryButton, Color secondaryButton, Color border, Color menuBarBg, String themeName) {            this.background = background;

            this.textAreaBg = textAreaBg;
            this.outputTextAreaBg = outputTextAreaBg;
            this.foreground = foreground;
            this.primaryButton = primaryButton;
            this.secondaryButton = secondaryButton;
            this.border = border;
            this.menuBarBg = menuBarBg;
            this.themeName = themeName;
        }
    }

    private enum ThemeMode {
        LIGHT, DARK
    }

    public enum Language {
        KOREAN, ENGLISH
    }

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
    private JMenu languageMenu;
    private JRadioButtonMenuItem koreanMenuItem;
    private JRadioButtonMenuItem englishMenuItem;
    private JMenu helpMenu;
    private JMenuItem aboutMenuItem;
    private JMenuItem customizeThemeItem;


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

        // Load and apply last used settings
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

            boolean isPressed = getModel().isArmed();
            int yOffset = isPressed ? 1 : 0;

            // Draw the darker "bottom edge" layer.
            g2.setColor(getBackground().darker());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // Determine the color for the top layer
            Color topColor;
            if (getModel().isRollover() && !isPressed) {
                topColor = getBackground().brighter();
            } else {
                topColor = getBackground();
            }

            // Draw the main, top layer of the button.
            // When not pressed, it's 1px shorter, revealing the bottom edge.
            // When pressed, it's shifted down 1px and covers the edge.
            g2.setColor(topColor);
            g2.fillRoundRect(0, yOffset, getWidth(), getHeight() - 1 - yOffset, cornerRadius, cornerRadius);

            // Let the superclass paint the text
            super.paintComponent(g);

            g2.dispose();
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

            Color bgColor = getViewport().getView().getBackground();

            // 1. Draw the darker "bottom edge" layer, similar to RoundedButton.
            g2.setColor(bgColor.darker());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

            // 2. Draw the main, top layer. It's 1px shorter, revealing the bottom edge.
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight() - 1, cornerRadius, cornerRadius);

            // Let the original scrollpane paint its content (the text area).
            super.paintComponent(g);

            g2.dispose();
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

        // Load theme, default to LIGHT
        String themeName = prefs.get("theme", ThemeMode.LIGHT.name());
        ThemeMode themeMode = ThemeMode.LIGHT;
        try { themeMode = ThemeMode.valueOf(themeName); } catch (IllegalArgumentException e) { /* Use default */ }

        // Load language, default to KOREAN
        String langName = prefs.get("language", Language.KOREAN.name());
        Language language = Language.KOREAN;
        try { language = Language.valueOf(langName); } catch (IllegalArgumentException e) { /* Use default */ }

        setTheme(themeMode);
        setLanguage(language);

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

                // For better taskbar integration on supported systems (like Windows, macOS),
                // explicitly set the icon there too using the modern Taskbar API (Java 9+).
                // It's often best to use the largest available icon.
                if (Taskbar.isTaskbarSupported()) {
                    try {
                        Taskbar.getTaskbar().setIconImage(icons.get(icons.size() - 1));
                    } catch (UnsupportedOperationException | SecurityException e) {
                        System.err.println("Could not set taskbar icon: " + e.getMessage());
                    }
                }
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
        ButtonGroup themeGroup = new ButtonGroup();

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

        customizeThemeItem = new JMenuItem();
        customizeThemeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String bgTitle = languageManager.get("CUSTOMIZE_THEME_BG_TITLE");
                String btnTitle = languageManager.get("CUSTOMIZE_THEME_BTN_TITLE");

                // Example:
                Color backgroundColor = JColorChooser.showDialog(AtbashCipherGUI.this, bgTitle, mainPanel.getBackground());
                Color buttonColor = JColorChooser.showDialog(AtbashCipherGUI.this, btnTitle, transformButton.getBackground());

                // Apply the selected colors to the theme
                if (backgroundColor != null) {
                    mainPanel.setBackground(backgroundColor);
                }
                if (buttonColor != null) {
                    configureFlatButton(transformButton, transformButton.getFont(), buttonColor);
                }
                SwingUtilities.updateComponentTreeUI(AtbashCipherGUI.this);
            }
        });
        themeMenu.add(customizeThemeItem);

        languageMenu = new JMenu();
        ButtonGroup languageGroup = new ButtonGroup();

        koreanMenuItem = new JRadioButtonMenuItem();
        koreanMenuItem.setSelected(true);
        koreanMenuItem.addActionListener(e -> setLanguage(Language.KOREAN));

        englishMenuItem = new JRadioButtonMenuItem();
        englishMenuItem.addActionListener(e -> setLanguage(Language.ENGLISH));

        languageGroup.add(koreanMenuItem);
        languageGroup.add(englishMenuItem);
        languageMenu.add(koreanMenuItem);
        languageMenu.add(englishMenuItem);

        helpMenu = new JMenu();
        aboutMenuItem = new JMenuItem();
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        menuBar.add(languageMenu);
        menuBar.add(helpMenu);

        setJMenuBar(menuBar);
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
        // Update radio button selection state
        koreanMenuItem.setSelected(lang == Language.KOREAN);
        englishMenuItem.setSelected(lang == Language.ENGLISH);

        // Save the preference
        if (saveSettingsEnabled) {
            prefs.put("language", lang.name());
        }

        languageManager.loadLanguage(lang);
        applyLanguage();
    }

    private void applyLanguage() {
        setTitle(languageManager.get("WINDOW_TITLE"));
        fileMenu.setText(languageManager.get("FILE_MENU"));
        saveMenuItem.setText(languageManager.get("SAVE_MENU"));
        saveAsImageMenuItem.setText(languageManager.get("SAVE_IMAGE_MENU"));
        loadMenuItem.setText(languageManager.get("LOAD_MENU"));
        saveSettingsMenuItem.setText(languageManager.get("SAVE_SETTINGS_MENU"));

        themeMenu.setText(languageManager.get("THEME_MENU"));
        lightMenuItem.setText(languageManager.get("LIGHT_MODE_MENU"));
        darkMenuItem.setText(languageManager.get("DARK_MODE_MENU"));
        customizeThemeItem.setText(languageManager.get("CUSTOMIZE_THEME"));
        languageMenu.setText(languageManager.get("LANGUAGE_MENU"));
        koreanMenuItem.setText(languageManager.get("KOREAN_MENU"));
        englishMenuItem.setText(languageManager.get("ENGLISH_MENU"));
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
        UIManager.put("nimbusSelectionBackground", theme.primaryButton);
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

        // For dark mode, use pure white for control text to improve contrast.
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
        configureFlatButton(transformButton, transformButton.getFont(), theme.primaryButton);
        configureFlatButton(clearButton, clearButton.getFont(), theme.secondaryButton);
        configureFlatButton(copyButton, copyButton.getFont(), theme.secondaryButton);

        // Menu Bar
        if (menuBar != null) {
            menuBar.setBackground(theme.menuBarBg);
            // Add vertical padding and a bottom shadow to make it more prominent
            Border padding = BorderFactory.createEmptyBorder(4, 0, 4, 0);
            Border shadow = BorderFactory.createMatteBorder(0, 0, 1, 0, theme.menuBarBg.darker());
            menuBar.setBorder(BorderFactory.createCompoundBorder(padding, shadow));
            for (int i = 0; i < menuBar.getMenuCount(); i++) {
                JMenu menu = menuBar.getMenu(i);
                if (menu != null) {
                    menu.setOpaque(false); // Let the menu bar background show through
                    menu.setForeground(toolbarRelatedColor);
                }
            }
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

    private void configureSpinner(JSpinner spinner, Theme theme, Color controlTextColor) {
        if (spinner == null) return;
        // Remove the border from the spinner container itself to avoid double borders
        spinner.setBorder(BorderFactory.createEmptyBorder());
        // Style the editor component which contains the text field
        JComponent editor = spinner.getEditor();
        editor.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)
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
                // 먼저 기본 렌더러를 호출하여 선택된 항목의 색상 등을 처리하게 합니다.
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                // 선택되지 않은 항목의 경우, 배경을 흰색으로, 글자색을 어둡게 강제합니다.
                if (!isSelected) {
                    setBackground(Color.WHITE);
                    setForeground(Theme.LIGHT.foreground);
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

    private void configureFlatButton(JButton button, Font font, Color backgroundColor) {
        button.setFont(font);
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setMargin(new Insets(8, 20, 8, 20));
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
            String inputText = inputTextArea.getText();
            String transformedText = "";
            int selectedCipherIndex = cipherSelector.getSelectedIndex();
            int key = 0;
            int key2 = 0;
            if (parameterPanel.isVisible()) {
                try {
                    // Commit edit to ensure the latest value is retrieved from the spinner's text field
                    parameterSpinner.commitEdit();
                    key = (Integer) parameterSpinner.getValue();
                    if (parameterSpinner2.isVisible()) {
                        parameterSpinner2.commitEdit();
                        key2 = (Integer) parameterSpinner2.getValue();
                    }
                } catch (java.text.ParseException ex) {
                    // Handle invalid input in spinner (e.g., non-numeric text)
                    JOptionPane.showMessageDialog(this, languageManager.get("INVALID_PARAMETER_ERROR"), languageManager.get("INPUT_ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

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
            outputTextArea.setText(transformedText);
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

                // Show feedback message
                feedbackLabel.setText(languageManager.get("COPY_FEEDBACK"));

                // Stop any existing timer to reset the hide delay
                if (feedbackTimer != null && feedbackTimer.isRunning()) {
                    feedbackTimer.stop();
                }

                // Create and start a timer to hide the message after 2 seconds
                feedbackTimer = new Timer(2000, event -> feedbackLabel.setText(""));
                feedbackTimer.setRepeats(false); // Ensure it only runs once
                feedbackTimer.start();
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

                Theme currentTheme = (darkMenuItem.isSelected()) ? Theme.DARK : Theme.LIGHT;
                g2d.setColor(currentTheme.outputTextAreaBg);
                g2d.fillRect(0, 0, imageWidth, imageHeight);

                g2d.setColor(currentTheme.foreground);
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
        // It's safer to run UI-related tasks on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(() -> {
            try {
                // 시스템 기본 LookAndFeel을 사용하여 OS와의 일체감을 높입니다.
                // 참고: 이전에 한글 입력 문제 해결을 위해 'Metal' L&F를 사용했으나,
                // 시각적으로 더 나은 시스템 기본 L&F로 되돌립니다.
                // 만약 일부 시스템에서 한글 입력이 다시 불안정해진다면 이 부분을 되돌릴 수 있습니다.
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            AtbashCipherGUI frame = new AtbashCipherGUI();
            frame.setVisible(true);
        });
    }
}