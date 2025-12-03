/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package baitap;

/**
 *
 * @author Admin
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.util.*;
import java.util.List;

/* ===================== MAIN APP ===================== */
public class SearchAppNoLogin {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}

/* ===================== MAIN FRAME ===================== */
class MainFrame extends JFrame {
    private JTextField keywordField;
    private JButton searchBtn, bookmarksBtn, compareBtn;
    private DefaultListModel<String> resultListModel = new DefaultListModel<>();
    private JList<String> resultList = new JList<>(resultListModel);
    private List<SearchResult> allResults = new ArrayList<>();
    private List<SearchResult> bookmarks = new ArrayList<>();

    public MainFrame() {
        setTitle("Search App No Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new FlowLayout());
        keywordField = new JTextField(30);
        searchBtn = new JButton("Tìm kiếm");
        bookmarksBtn = new JButton("Bookmarks");
        compareBtn = new JButton("So sánh giá");

        topPanel.add(new JLabel("Nhập từ khóa:"));
        topPanel.add(keywordField);
        topPanel.add(searchBtn);
        topPanel.add(bookmarksBtn);
        topPanel.add(compareBtn);

        add(topPanel, BorderLayout.NORTH);

        resultList.setFixedCellHeight(30);
        JScrollPane scroll = new JScrollPane(resultList);
        add(scroll, BorderLayout.CENTER);

        // sự kiện tìm kiếm
        searchBtn.addActionListener(e -> performSearch());
        keywordField.addActionListener(e -> performSearch());

        // bookmark
        bookmarksBtn.addActionListener(e -> new BookmarksDialog(this, bookmarks).setVisible(true));

        // so sánh
        compareBtn.addActionListener(e -> new CompareDialog(this, allResults).setVisible(true));

        // mở web khi double-click
        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = resultList.locationToIndex(e.getPoint());
                    if (index >= 0 && index < allResults.size()) {
                        openInBrowser(allResults.get(index).url);
                    }
                }
            }
        });

        setVisible(true);
    }

    private void performSearch() {
        String keyword = keywordField.getText().trim();
        if (keyword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập từ khóa trước khi tìm kiếm");
            return;
        }

        // giả lập kết quả từ nhiều site
        allResults.clear();
        for (int i = 1; i <= 20; i++) {
            allResults.add(new SearchResult("Site" + ((i % 3) + 1),
                    keyword + " sản phẩm " + i,
                    500_000 + i * 10_000,
                    "https://example.com/product" + i));
        }

        resultListModel.clear();
        for (SearchResult r : allResults) {
            resultListModel.addElement(r.site + " - " + r.title + " - " + r.formattedPrice());
        }
    }

    private void openInBrowser(String url) {
        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không mở được trình duyệt: " + ex.getMessage());
        }
    }
}

/* ===================== SEARCH RESULT ===================== */
class SearchResult {
    String site;
    String title;
    long price;
    String url;

    public SearchResult(String site, String title, long price, String url) {
        this.site = site;
        this.title = title;
        this.price = price;
        this.url = url;
    }

    public String formattedPrice() {
        return String.format("%,d đ", price).replace(',', '.');
    }
}

/* ===================== BOOKMARKS DIALOG ===================== */
class BookmarksDialog extends JDialog {
    private List<SearchResult> bookmarks;
    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> list;

    public BookmarksDialog(JFrame owner, List<SearchResult> bookmarks) {
        super(owner, "Bookmarks", true);
        this.bookmarks = bookmarks;

        setSize(600, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        list = new JList<>(listModel);
        JScrollPane scroll = new JScrollPane(list);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton deleteBtn = new JButton("Xóa");
        JButton closeBtn = new JButton("Đóng");
        bottom.add(deleteBtn);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        fillData();

        deleteBtn.addActionListener(e -> deleteSelected());
        closeBtn.addActionListener(e -> dispose());

        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index >= 0 && index < bookmarks.size()) {
                        try {
                            Desktop.getDesktop().browse(new URI(bookmarks.get(index).url));
                        } catch (Exception ex) { }
                    }
                }
            }
        });
    }

    private void fillData() {
        listModel.clear();
        for (SearchResult r : bookmarks) {
            listModel.addElement(r.site + " - " + r.title + " - " + r.formattedPrice());
        }
    }

    private void deleteSelected() {
        int index = list.getSelectedIndex();
        if (index >= 0) {
            bookmarks.remove(index);
            fillData();
        } else {
            JOptionPane.showMessageDialog(this, "Chọn mục để xóa");
        }
    }
}

/* ===================== COMPARE DIALOG ===================== */
class CompareDialog extends JDialog {
    private List<SearchResult> results;
    private JComboBox<String> site1Combo;
    private JComboBox<String> site2Combo;
    private JLabel compareResultLabel;

    public CompareDialog(JFrame owner, List<SearchResult> results) {
        super(owner, "So sánh giá", true);
        this.results = results;

        setSize(700, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout());
        site1Combo = new JComboBox<>();
        site2Combo = new JComboBox<>();

        Set<String> sites = new LinkedHashSet<>();
        for (SearchResult r : results) sites.add(r.site);
        for (String s : sites) {
            site1Combo.addItem(s);
            site2Combo.addItem(s);
        }

        JButton compareBtn = new JButton("So sánh");
        topPanel.add(new JLabel("Web 1:")); topPanel.add(site1Combo);
        topPanel.add(new JLabel("Web 2:")); topPanel.add(site2Combo);
        topPanel.add(compareBtn);

        compareResultLabel = new JLabel(" ");
        compareResultLabel.setVerticalAlignment(SwingConstants.TOP);

        add(topPanel, BorderLayout.NORTH);
        add(compareResultLabel, BorderLayout.CENTER);

        compareBtn.addActionListener(e -> compareSites());
    }

    private void compareSites() {
        String s1 = (String) site1Combo.getSelectedItem();
        String s2 = (String) site2Combo.getSelectedItem();
        if (s1.equals(s2)) {
            JOptionPane.showMessageDialog(this, "Chọn 2 site khác nhau");
            return;
        }

        SearchResult r1 = getCheapest(s1);
        SearchResult r2 = getCheapest(s2);

        if (r1 == null || r2 == null) return;

        long diff = Math.abs(r1.price - r2.price);
        String cheaper = r1.price < r2.price ? r1.site : r2.site;

        compareResultLabel.setText("<html>" +
                s1 + ": " + r1.formattedPrice() + "<br>" +
                s2 + ": " + r2.formattedPrice() + "<br><br>" +
                "Site rẻ hơn: " + cheaper + " (chênh lệch " + String.format("%,d đ", diff).replace(',', '.') + ")" +
                "</html>");
    }

    private SearchResult getCheapest(String site) {
        SearchResult best = null;
        for (SearchResult r : results) {
            if (!r.site.equals(site)) continue;
            if (best == null || r.price < best.price) best = r;
        }
        return best;
    }
}
