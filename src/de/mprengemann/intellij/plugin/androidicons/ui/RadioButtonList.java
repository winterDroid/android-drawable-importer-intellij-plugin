package de.mprengemann.intellij.plugin.androidicons.ui;

import com.intellij.ui.ClickListener;
import com.intellij.ui.components.JBList;
import com.intellij.util.Function;
import com.intellij.util.containers.BidirectionalMap;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

public class RadioButtonList<T> extends JBList {

    private static final int DEFAULT_RADIO_WIDTH = 20;
    private RadioListListener radioListListener;
    private final BidirectionalMap<T, JRadioButton> myItemMap = new BidirectionalMap<T, JRadioButton>();
    private final ButtonGroup buttonGroup = new ButtonGroup();

    public RadioButtonList(final RadioListListener radioListListener) {
        this(new DefaultListModel(), radioListListener);
    }

    public RadioButtonList(final DefaultListModel dataModel, final RadioListListener radioListListener) {
        this(dataModel);
        setRadioListListener(radioListListener);
    }

    public RadioButtonList() {
        this(new DefaultListModel());
    }

    public RadioButtonList(final DefaultListModel dataModel) {
        super();
        //noinspection unchecked
        setModel(dataModel);
        setCellRenderer(new CellRenderer());
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setBorder(BorderFactory.createEtchedBorder());
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ' ') {
                    for (int index : getSelectedIndices()) {
                        if (index >= 0) {
                            JRadioButton radioButton = (JRadioButton) getModel().getElementAt(index);
                            setSelected(radioButton, index);
                        }
                    }
                }
            }
        });
        new ClickListener() {
            @Override
            public boolean onClick(@NotNull MouseEvent e, int clickCount) {
                if (isEnabled()) {
                    int index = locationToIndex(e.getPoint());

                    if (index != -1) {
                        JRadioButton radioButton = (JRadioButton) getModel().getElementAt(index);
                        int iconArea;
                        try {
                            iconArea = radioButton.getMargin().left +
                                       ((BasicRadioButtonUI) radioButton.getUI()).getDefaultIcon().getIconWidth() +
                                       radioButton.getIconTextGap();
                        } catch (ClassCastException c) {
                            iconArea = DEFAULT_RADIO_WIDTH;
                        }
                        if (e.getX() < iconArea) {
                            setSelected(radioButton, index);
                            return true;
                        }
                    }
                }
                return false;
            }
        }.installOn(this);
    }

    public void setItems(final List<T> items, @Nullable Function<T, String> converter) {
        clear();
        for (T item : items) {
            String text = converter != null ? converter.fun(item) : item.toString();
            addItem(item, text, false);
        }
    }

    public void addItem(T item, String text, boolean selected) {
        JRadioButton checkBox = new JRadioButton(text, selected);
        if (myItemMap.isEmpty()) {
            checkBox.setSelected(true);
        }
        myItemMap.put(item, checkBox);
        buttonGroup.add(checkBox);
        //noinspection unchecked
        ((DefaultListModel) getModel()).addElement(checkBox);
    }

    public void clear() {
        ((DefaultListModel) getModel()).clear();
        myItemMap.clear();
    }

    private void setSelected(JRadioButton radioButton, int index) {
        boolean value = !radioButton.isSelected();
        radioButton.setSelected(value);
        repaint();

        // fire change notification in case if we've already initialized model
        final ListModel model = getModel();
        if (model instanceof DefaultListModel) {
            //noinspection unchecked
            ((DefaultListModel) model).setElementAt(getModel().getElementAt(index), index);
        }

        if (radioListListener != null) {
            radioListListener.radioSelectionChanged(index);
        }
    }

    public void setRadioListListener(RadioListListener checkBoxListListener) {
        this.radioListListener = checkBoxListListener;
    }

    protected void adjustRendering(final JRadioButton checkBox, final boolean selected, final boolean hasFocus) {
    }

    private class CellRenderer implements ListCellRenderer {
        private final Border mySelectedBorder;
        private final Border myBorder;

        private CellRenderer() {
            mySelectedBorder = UIManager.getBorder("List.focusCellHighlightBorder");
            final Insets borderInsets = mySelectedBorder.getBorderInsets(new JCheckBox());
            myBorder = new EmptyBorder(borderInsets);
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            JRadioButton radioButton = (JRadioButton) value;
            if (!UIUtil.isUnderNimbusLookAndFeel()) {
                radioButton.setBackground(getBackground(isSelected, radioButton));
                radioButton.setForeground(getForeground(isSelected, radioButton));
            }
            radioButton.setEnabled(isEnabled());
            radioButton.setFont(getFont(radioButton));
            radioButton.setFocusPainted(false);
            radioButton.setBorderPainted(true);
            radioButton.setBorder(isSelected ? mySelectedBorder : myBorder);
            adjustRendering(radioButton, isSelected, cellHasFocus);
            return radioButton;
        }
    }

    protected Font getFont(final JRadioButton checkbox) {
        return getFont();
    }

    protected Color getBackground(final boolean isSelected, final JRadioButton checkbox) {
        return isSelected ? getSelectionBackground() : getBackground();
    }

    protected Color getForeground(final boolean isSelected, final JRadioButton checkbox) {
        return isSelected ? getSelectionForeground() : getForeground();
    }

}
