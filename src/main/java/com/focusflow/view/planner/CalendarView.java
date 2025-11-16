package com.focusflow.view.planner;

import com.focusflow.model.planner.Planner;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.focusflow.observer.Event;
import com.focusflow.observer.Observer;

/**
 * Calendar view component for displaying and navigating dates.
 *
 * @author Gianluca Binetti
 */
public class CalendarView extends JPanel implements Observer {

    private Calendar currentCalendar;
    private JLabel monthYearLabel;
    private JPanel calendarGrid;
    private JScrollPane scrollPane;
    private Planner planner;
    private boolean isWeekView = false;

    private static final String[] DAY_NAMES = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
    private static final SimpleDateFormat MONTH_FORMAT = new SimpleDateFormat("MMMM yyyy");

    /**
     * Creates a new CalendarView.
     */
    public CalendarView() {
        currentCalendar = Calendar.getInstance();
        initializeUI();
        showMonth(currentCalendar.getTime());
    }

    /**
     * Initializes the UI components.
     */
    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel navPanel = createNavigationPanel();
        add(navPanel, BorderLayout.NORTH);

        calendarGrid = new JPanel();
        scrollPane = new JScrollPane(calendarGrid);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Creates the navigation panel.
     */
    private JPanel createNavigationPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JButton prevButton = new JButton("<");
        JButton nextButton = new JButton(">");
        monthYearLabel = new JLabel("", SwingConstants.CENTER);
        monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        prevButton.addActionListener(e -> {
            if (isWeekView)
                previousWeek();
            else
                previousMonth();
        });
        nextButton.addActionListener(e -> {
            if (isWeekView)
                nextWeek();
            else
                nextMonth();
        });

        panel.add(prevButton, BorderLayout.WEST);
        panel.add(monthYearLabel, BorderLayout.CENTER);
        panel.add(nextButton, BorderLayout.EAST);

        return panel;
    }

    /**
     * Shows the calendar for a specific month.
     */
    public void showMonth(Date date) {
        isWeekView = false;
        currentCalendar.setTime(date);
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);

        monthYearLabel.setText(MONTH_FORMAT.format(currentCalendar.getTime()));
        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridLayout(0, 7, 2, 2));

        for (String dayName : DAY_NAMES) {
            JLabel label = new JLabel(dayName, SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 11));
            label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            calendarGrid.add(label);
        }

        int firstDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGrid.add(createEmptyDayCell());
        }

        Calendar today = Calendar.getInstance();
        for (int day = 1; day <= daysInMonth; day++) {
            boolean isToday = currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    day == today.get(Calendar.DAY_OF_MONTH);
            calendarGrid.add(createDayCell(day, isToday));
        }

        int totalCells = 42;
        int usedCells = firstDayOfWeek + daysInMonth;
        for (int i = usedCells; i < totalCells; i++) {
            calendarGrid.add(createEmptyDayCell());
        }

        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    /**
     * Creates an empty day cell for padding.
     */
    private JPanel createEmptyDayCell() {
        JPanel cell = new JPanel();
        cell.setPreferredSize(new Dimension(80, 70));
        cell.setBorder(new LineBorder(Color.LIGHT_GRAY));
        cell.setBackground(new Color(245, 245, 245));
        return cell;
    }

    /**
     * Creates a day cell with day number and clickable events.
     */
    private JPanel createDayCell(int day, boolean isToday) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setPreferredSize(new Dimension(80, 70));
        cell.setBorder(new LineBorder(Color.LIGHT_GRAY));
        cell.setBackground(Color.WHITE);

        JLabel dayLabel = new JLabel(String.valueOf(day));
        dayLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        if (isToday) {
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
            dayLabel.setForeground(Color.BLUE);
        }
        cell.add(dayLabel, BorderLayout.NORTH);

        List<Planner.PlannerEvent> events = getEventsForDay(day);
        if (!events.isEmpty()) {
            JPanel eventPanel = new JPanel();
            eventPanel.setLayout(new BoxLayout(eventPanel, BoxLayout.Y_AXIS));
            eventPanel.setOpaque(false);

            int maxEvents = Math.min(events.size(), 3);
            for (int i = 0; i < maxEvents; i++) {
                Planner.PlannerEvent event = events.get(i);
                JLabel eventLabel = createClickableEventLabel(event, 12);
                eventPanel.add(eventLabel);
            }
            if (events.size() > 3) {
                JLabel moreLabel = new JLabel("+" + (events.size() - 3) + " more");
                moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
                moreLabel.setForeground(Color.GRAY);
                moreLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                final List<Planner.PlannerEvent> allEvents = events;
                moreLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        showAllEventsForDay(allEvents);
                        e.consume();
                    }
                });
                eventPanel.add(moreLabel);
            }
            cell.add(eventPanel, BorderLayout.CENTER);
        }

        return cell;
    }

    /**
     * Shows a week view with time grid.
     */
    public void showWeek(Date date) {
        isWeekView = true;
        currentCalendar.setTime(date);
        int dayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        currentCalendar.add(Calendar.DAY_OF_MONTH, -(dayOfWeek - 1));

        SimpleDateFormat weekFormat = new SimpleDateFormat("MMM d");
        Calendar endWeek = (Calendar) currentCalendar.clone();
        endWeek.add(Calendar.DAY_OF_MONTH, 6);

        monthYearLabel.setText("Week of " + weekFormat.format(currentCalendar.getTime()) +
                " - " + weekFormat.format(endWeek.getTime()));

        calendarGrid.removeAll();
        calendarGrid.setLayout(new BorderLayout());

        JPanel weekPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.05;
        weekPanel.add(new JLabel(""), gbc);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE M/d");
        Calendar weekDay = (Calendar) currentCalendar.clone();
        Calendar today = Calendar.getInstance();

        for (int i = 0; i < 7; i++) {
            gbc.gridx = i + 1;
            gbc.weightx = 1.0 / 7;
            JLabel header = new JLabel(dayFormat.format(weekDay.getTime()), SwingConstants.CENTER);
            header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            if (weekDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    weekDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                header.setFont(header.getFont().deriveFont(Font.BOLD));
                header.setForeground(Color.BLUE);
            }
            weekPanel.add(header, gbc);
            weekDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (int hour = 0; hour < 24; hour++) {
            gbc.gridy = hour + 1;
            gbc.gridx = 0;
            gbc.weightx = 0.05;
            gbc.weighty = 1.0;
            String timeStr;
            if (hour == 0)
                timeStr = "12AM";
            else if (hour < 12)
                timeStr = hour + "AM";
            else if (hour == 12)
                timeStr = "12PM";
            else
                timeStr = (hour - 12) + "PM";
            JLabel timeLabel = new JLabel(timeStr, SwingConstants.RIGHT);
            timeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            weekPanel.add(timeLabel, gbc);

            weekDay = (Calendar) currentCalendar.clone();
            for (int dayIdx = 0; dayIdx < 7; dayIdx++) {
                gbc.gridx = dayIdx + 1;
                gbc.weightx = 1.0 / 7;

                JPanel hourCell = createHourCell(weekDay.getTime(), hour);
                weekPanel.add(hourCell, gbc);

                weekDay.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        calendarGrid.add(weekPanel, BorderLayout.CENTER);
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    /**
     * Creates a cell for a specific hour in the week view.
     */
    private JPanel createHourCell(Date date, int hour) {
        JPanel cell = new JPanel();
        cell.setLayout(new BoxLayout(cell, BoxLayout.Y_AXIS));
        cell.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
        cell.setBackground(Color.WHITE);
        cell.setPreferredSize(new Dimension(80, 30));

        List<Planner.PlannerEvent> events = getEventsForDate(date);
        List<Planner.PlannerEvent> hourEvents = new java.util.ArrayList<>();
        for (Planner.PlannerEvent event : events) {
            if (event.getStartTime() != null && event.getStartTime().getHour() == hour) {
                hourEvents.add(event);
            }
        }

        int maxEvents = Math.min(hourEvents.size(), 2);
        for (int i = 0; i < maxEvents; i++) {
            cell.add(createClickableEventLabel(hourEvents.get(i), 15));
        }

        if (hourEvents.size() > 2) {
            JLabel moreLabel = new JLabel("+" + (hourEvents.size() - 2));
            moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
            moreLabel.setForeground(Color.GRAY);
            moreLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            final List<Planner.PlannerEvent> allEvents = hourEvents;
            moreLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    showAllEventsForDay(allEvents);
                    e.consume();
                }
            });
            cell.add(moreLabel);
        }

        return cell;
    }

    /**
     * Navigates to the previous month.
     */
    public void previousMonth() {
        currentCalendar.add(Calendar.MONTH, -1);
        showMonth(currentCalendar.getTime());
    }

    /**
     * Navigates to the next month.
     */
    public void nextMonth() {
        currentCalendar.add(Calendar.MONTH, 1);
        showMonth(currentCalendar.getTime());
    }

    /**
     * Navigates to the previous week.
     */
    public void previousWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, -1);
        showWeek(currentCalendar.getTime());
    }

    /**
     * Navigates to the next week.
     */
    public void nextWeek() {
        currentCalendar.add(Calendar.WEEK_OF_YEAR, 1);
        showWeek(currentCalendar.getTime());
    }

    /**
     * Creates a clickable event label that shows details when clicked.
     */
    private JLabel createClickableEventLabel(Planner.PlannerEvent event, int maxLen) {
        JLabel label = new JLabel(truncate(event.getTitle(), maxLen));
        label.setFont(new Font("SansSerif", Font.PLAIN, 9));
        label.setForeground(new Color(0, 100, 0));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        label.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showEventDetails(event);
                e.consume();
            }
        });
        return label;
    }

    /**
     * Shows assignment details in a popup dialog.
     */
    private void showEventDetails(Planner.PlannerEvent event) {
        DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a");

        StringBuilder details = new StringBuilder();
        details.append(event.getTitle()).append("\n\n");

        if (event.getStartTime() != null) {
            details.append("Due: ").append(event.getStartTime().format(dateTimeFormat)).append("\n\n");
        }

        if (event.isStudyBlock()) {
            details.append("Mode: ").append(event.getTimerMode()).append("\n\n");
        }

        String description = event.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            details.append("Description:\n").append(description.replace("\\n", "\n").replace("\\,", ","));
        } else {
            details.append("No description available.");
        }

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        textArea.setBackground(null);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(350, 150));
        scrollPane.setBorder(null);

        Object[] options;
        if (event.isStudyBlock()) {
            options = new Object[] { "Start Session", "OK" };
        } else {
            options = new Object[] { "OK" };
        }

        int result = JOptionPane.showOptionDialog(this, scrollPane, "Details",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        if (event.isStudyBlock() && result == 0) {
            // Start session
            com.focusflow.view.MainFrame frame = (com.focusflow.view.MainFrame) SwingUtilities.getWindowAncestor(this);
            if (frame != null) {
                frame.startStudySession(event);
            }
        }
    }

    /**
     * Shows all events for a day when "+X more" is clicked.
     */
    private void showAllEventsForDay(List<Planner.PlannerEvent> events) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("h:mm a");

        String[] columnNames = { "Assignment", "Due Time" };
        Object[][] data = new Object[events.size()][2];

        for (int i = 0; i < events.size(); i++) {
            Planner.PlannerEvent event = events.get(i);
            data[i][0] = event.getTitle();
            data[i][1] = event.getStartTime() != null ? event.getStartTime().format(timeFormat) : "N/A";
        }

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(25);
        table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.getSelectedRow();
                    if (row >= 0 && row < events.size()) {
                        showEventDetails(events.get(row));
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        JOptionPane.showMessageDialog(this, scrollPane, "All Assignments",
                JOptionPane.PLAIN_MESSAGE);
    }

    public void setPlanner(Planner planner) {
        this.planner = planner;
        planner.addObserver(this);
        refresh();
    }

    public void refresh() {
        if (isWeekView) {
            showWeek(currentCalendar.getTime());
        } else {
            showMonth(currentCalendar.getTime());
        }
    }

    private List<Planner.PlannerEvent> getEventsForDay(int day) {
        if (planner == null)
            return List.of();
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, day);
        LocalDate localDate = cal.getTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return planner.getEventsForDate(localDate);
    }

    private List<Planner.PlannerEvent> getEventsForDate(Date date) {
        if (planner == null)
            return List.of();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return planner.getEventsForDate(localDate);
    }

    private String truncate(String text, int maxLen) {
        if (text == null)
            return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 2) + ".." : text;
    }

    @Override
    public void update(Event event) {
        SwingUtilities.invokeLater(this::refresh);
    }
}
