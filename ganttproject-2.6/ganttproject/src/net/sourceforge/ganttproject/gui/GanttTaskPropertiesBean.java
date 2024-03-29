/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 GanttProject team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.IGanttProject;
import net.sourceforge.ganttproject.action.GPAction;
import net.sourceforge.ganttproject.gui.options.OptionsPageBuilder;
import net.sourceforge.ganttproject.gui.options.SpringUtilities;
import net.sourceforge.ganttproject.gui.taskproperties.CustomColumnsPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskAllocationsPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskDependenciesPanel;
import net.sourceforge.ganttproject.gui.taskproperties.TaskScheduleDatesPanel;
import net.sourceforge.ganttproject.language.GanttLanguage;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.shape.JPaintCombo;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.task.TaskImpl;
import net.sourceforge.ganttproject.task.TaskManager;
import net.sourceforge.ganttproject.task.TaskMutator;
import net.sourceforge.ganttproject.util.BrowserControl;
import net.sourceforge.ganttproject.util.collect.Pair;

import org.jdesktop.swingx.JXDatePicker;
import org.jdesktop.swingx.JXHyperlink;

import biz.ganttproject.core.chart.render.ShapeConstants;
import biz.ganttproject.core.chart.render.ShapePaint;
import biz.ganttproject.core.option.ColorOption;
import biz.ganttproject.core.option.DefaultColorOption;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;

import com.google.common.base.Objects;

/**
 * Real panel for editing task properties
 */
public class GanttTaskPropertiesBean extends JPanel {

  private ColorOption myTaskColorOption = new DefaultColorOption("");
  private final GPAction mySetDefaultColorAction = new GPAction("defaultColor") {
    @Override
    public void actionPerformed(ActionEvent e) {
      myTaskColorOption.setValue(myUIfacade.getGanttChart().getTaskDefaultColorOption().getValue());
    }
  };
  private JXDatePicker myThirdDatePicker;

  protected GanttTask[] selectedTasks;

  private static final GanttLanguage language = GanttLanguage.getInstance();

  private GanttCalendar myThird;

  private JTabbedPane tabbedPane; // TabbedPane that includes the following four
                                  // items

  private JPanel generalPanel;

  private JComponent predecessorsPanel;

  private JPanel resourcesPanel;

  private JPanel notesPanel;

  private JTextField nameField1;

  private JComboBox BarField;

  public String Pattern[] = {"LOW","MEDIUM","LARGE"};

  public static int Barsize=5;

  private JTextField tfWebLink;

  private JButton bWebLink;

  private JSpinner percentCompleteSlider;

  private JComboBox priorityComboBox;

  private JComboBox thirdDateComboBox;

  private JCheckBox mileStoneCheckBox1;

  private JCheckBox projectTaskCheckBox1;

  /** Shape chooser combo Box */
  private JPaintCombo shapeComboBox;

  private JScrollPane scrollPaneNotes;

  private JTextArea noteAreaNotes;

  private JPanel secondRowPanelNotes;

  private String originalName;

  private String originalWebLink;

  private boolean originalIsMilestone;

  private GanttCalendar originalStartDate;

  private GanttCalendar originalEndDate;

  private GanttCalendar originalThirdDate;

  private int originalThirdDateConstraint;

  private boolean originalIsProjectTask;

  private String originalNotes;

  private int originalCompletionPercentage;

  private Task.Priority originalPriority;

  private ShapePaint originalShape;

  private final TaskScheduleDatesPanel myTaskScheduleDates;

  private CustomColumnsPanel myCustomColumnPanel = null;

  private TaskDependenciesPanel myDependenciesPanel;

  private TaskAllocationsPanel myAllocationsPanel;

  private final HumanResourceManager myHumanResourceManager;

  private final RoleManager myRoleManager;

  private Task myUnpluggedClone;
  private final TaskManager myTaskManager;
  private final IGanttProject myProject;
  private final UIFacade myUIfacade;

  private JCheckBox myShowInTimeline;

  public GanttTaskPropertiesBean(GanttTask[] selectedTasks, IGanttProject project, UIFacade uifacade) {
    myTaskScheduleDates = new TaskScheduleDatesPanel(uifacade);
    this.selectedTasks = selectedTasks;
    storeOriginalValues(selectedTasks[0]);
    myHumanResourceManager = project.getHumanResourceManager();
    myRoleManager = project.getRoleManager();
    myTaskManager = project.getTaskManager();
    myProject = project;
    myUIfacade = uifacade;
    init();
    setSelectedTaskProperties();
  }

  private static void addEmptyRow(JPanel form) {
    form.add(Box.createRigidArea(new Dimension(1, 10)));
    form.add(Box.createRigidArea(new Dimension(1, 10)));
  }

  /** Construct the general panel */
  private void constructGeneralPanel() {
    final JPanel propertiesPanel = new JPanel(new SpringLayout());

    propertiesPanel.add(new JLabel(language.getText("name")));
    nameField1 = new JTextField(20);
    nameField1.setName("name_of_task");
    propertiesPanel.add(nameField1);

    /*Combobox for Resizing GanttBar */
    propertiesPanel.add(new JLabel("Barsize"));
    BarField = new JComboBox(Pattern);
    BarField.setName("size_of_task");
    propertiesPanel.add(BarField);

    /*if(BarField.getSelectedItem()=="Small")
    {
    	BarField.setSelectedItem(Pattern[0]);

  	  }
    else if(BarField.getSelectedItem()=="Medium")
    {

  	 BarField.setSelectedItem(pattern[1]);
    }
    else if(BarField.getSelectedItem()=="Large")
    {
    	BarField.setSelectedItem(pattern[2]);
    }*/


    Pair<String, JCheckBox> checkBox = constructCheckBox();
    if (checkBox != null) {
      propertiesPanel.add(new JLabel(checkBox.first()));
      propertiesPanel.add(checkBox.second());
    }
    addEmptyRow(propertiesPanel);

    myTaskScheduleDates.insertInto(propertiesPanel);

    Box extraConstraintBox = Box.createHorizontalBox();
    thirdDateComboBox = new JComboBox();
    thirdDateComboBox.addItem("");
    thirdDateComboBox.addItem(language.getText("earliestBegin"));
    thirdDateComboBox.setName("third");
    thirdDateComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        switch (thirdDateComboBox.getSelectedIndex()) {
        case TaskImpl.EARLIESTBEGIN:
          myThirdDatePicker.setEnabled(true);
          break;
        case TaskImpl.NONE:
          myThirdDatePicker.setEnabled(false);
          break;
        }
      }
    });
    extraConstraintBox.add(thirdDateComboBox);
    myThirdDatePicker = UIUtil.createDatePicker(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setThird(CalendarFactory.createGanttCalendar(((JXDatePicker) e.getSource()).getDate()), false);
      }
    });
    extraConstraintBox.add(Box.createHorizontalStrut(5));
    extraConstraintBox.add(myThirdDatePicker);
    propertiesPanel.add(new JLabel(language.getText("option.taskProperties.main.extraConstraint.label")));
    propertiesPanel.add(extraConstraintBox);

    addEmptyRow(propertiesPanel);

    propertiesPanel.add(new JLabel(language.getText("priority")));
    priorityComboBox = new JComboBox();
    for (Task.Priority p : Task.Priority.values()) {
      priorityComboBox.addItem(language.getText(p.getI18nKey()));
    }
    priorityComboBox.setEditable(false);
    propertiesPanel.add(priorityComboBox);

    propertiesPanel.add(new JLabel(language.getText("advancement")));
    SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, 100, 1);
    percentCompleteSlider = new JSpinner(spinnerModel);
    propertiesPanel.add(percentCompleteSlider);

    addEmptyRow(propertiesPanel);

    propertiesPanel.add(new JLabel(language.getText("option.taskProperties.main.showInTimeline.label")));
    myShowInTimeline = new JCheckBox();
    propertiesPanel.add(myShowInTimeline);

    propertiesPanel.add(new JLabel(language.getText("shape")));
    shapeComboBox = new JPaintCombo(ShapeConstants.PATTERN_LIST);
    propertiesPanel.add(shapeComboBox);

    OptionsPageBuilder builder = new OptionsPageBuilder(GanttTaskPropertiesBean.this, OptionsPageBuilder.TWO_COLUMN_LAYOUT);
    JPanel colorBox = new JPanel(new BorderLayout(5, 0));
    colorBox.add(builder.createColorComponent(myTaskColorOption), BorderLayout.WEST);
    //colorBox.add(Box.createHorizontalStrut(5));
    colorBox.add(new JXHyperlink(mySetDefaultColorAction), BorderLayout.CENTER);
    //colorBox.add(Box.createHorizontalGlue());
    //colorBox.add(Box.createHorizontalGlue());
    //colorBox.add(Box.createHorizontalGlue());

    propertiesPanel.add(new JLabel(language.getText("colors")));
    propertiesPanel.add(colorBox);

    Box weblinkBox = Box.createHorizontalBox();
    tfWebLink = new JTextField(20);
    weblinkBox.add(tfWebLink);
    weblinkBox.add(Box.createHorizontalStrut(2));
    bWebLink = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/web_16.gif")));
    bWebLink.setToolTipText(GanttProject.getToolTip(language.getText("openWebLink")));
    weblinkBox.add(bWebLink);

    bWebLink.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // link to open the web link
        if (!BrowserControl.displayURL(tfWebLink.getText())) {
          GanttDialogInfo gdi = new GanttDialogInfo(null, GanttDialogInfo.ERROR, GanttDialogInfo.YES_OPTION,
              language.getText("msg4"), language.getText("error"));
          gdi.setVisible(true);
        }
      }
    });
    propertiesPanel.add(new JLabel(language.getText("webLink")));
    propertiesPanel.add(weblinkBox);

    SpringUtilities.makeCompactGrid(propertiesPanel, propertiesPanel.getComponentCount() / 2, 2, 1, 1, 5, 5);

    JPanel propertiesWrapper = new JPanel(new BorderLayout());
    propertiesWrapper.add(propertiesPanel, BorderLayout.NORTH);
    generalPanel = new JPanel(new SpringLayout());
    //generalPanel.add(new JLayer<JPanel>(propertiesPanel, layerUi));
    generalPanel.add(propertiesWrapper);
    generalPanel.add(notesPanel);
    SpringUtilities.makeCompactGrid(generalPanel, 1, 2, 1, 1, 10, 5);
    generalPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }

  private void constructCustomColumnPanel() {
    myCustomColumnPanel = new CustomColumnsPanel(myProject.getTaskCustomColumnManager(), myUIfacade,
        selectedTasks[0].getCustomValues(), myUIfacade.getTaskTree().getVisibleFields());
  }

  /** Construct the predecessors tabbed pane */
  private void constructPredecessorsPanel() {
    myDependenciesPanel = new TaskDependenciesPanel();
    myDependenciesPanel.init(selectedTasks[0]);
    predecessorsPanel = myDependenciesPanel.getComponent();
  }

  /** Construct the resources panel */
  private void constructResourcesPanel() {
    myAllocationsPanel = new TaskAllocationsPanel(selectedTasks[0], myHumanResourceManager, myRoleManager);
    resourcesPanel = myAllocationsPanel.getComponent();
  }

  /** Construct the notes panel */
  private void constructNotesPanel() {
    secondRowPanelNotes = new JPanel(new BorderLayout());
    UIUtil.createTitle(secondRowPanelNotes, language.getText("notesTask"));

    noteAreaNotes = new JTextArea(8, 40);
    noteAreaNotes.setLineWrap(true);
    noteAreaNotes.setWrapStyleWord(true);
    noteAreaNotes.setBackground(new Color(1.0f, 1.0f, 1.0f));

    scrollPaneNotes = new JScrollPane(noteAreaNotes);
    secondRowPanelNotes.add(scrollPaneNotes, BorderLayout.CENTER);
    notesPanel = secondRowPanelNotes;
  }

  /** Initialize the widgets */
  private void init() {
    constructNotesPanel();

    tabbedPane = new JTabbedPane();
//    tabbedPane.getModel().addChangeListener(new ChangeListener() {
//      @Override
//      public void stateChanged(ChangeEvent e) {
//        changeNameOfTask();
//        fireDurationChanged();
//      }
//    });
    constructGeneralPanel();

    tabbedPane.addTab(language.getText("general"), new ImageIcon(getClass().getResource("/icons/properties_16.gif")),
        generalPanel);

    constructPredecessorsPanel();
    tabbedPane.addTab(language.getText("predecessors"), new ImageIcon(getClass().getResource("/icons/relashion.gif")),
        predecessorsPanel);

    constructResourcesPanel();

    tabbedPane.addTab(language.getCorrectedLabel("human"), new ImageIcon(getClass().getResource("/icons/res_16.gif")),
        resourcesPanel);

    setLayout(new BorderLayout());

    add(tabbedPane, BorderLayout.CENTER);

    constructCustomColumnPanel();
    tabbedPane.addTab(language.getText("customColumns"), new ImageIcon(getClass().getResource("/icons/custom.gif")),
        myCustomColumnPanel.getComponent());
    tabbedPane.addFocusListener(new FocusAdapter() {
      private boolean isFirstFocusGain = true;

      @Override
      public void focusGained(FocusEvent e) {
        super.focusGained(e);
        if (isFirstFocusGain) {
          nameField1.requestFocus();
          isFirstFocusGain = false;
        }
      }
    });
    tabbedPane.setBorder(BorderFactory.createEmptyBorder(2, 0, 5, 0));
  }

  /** Apply the modified properties to the selected Tasks */
  public void applySettings() {


    for (int i = 0; i < selectedTasks.length; i++) {
      // TODO The originalXXX values should not be used,
      // but the original values should be read from each processed task to
      // determine whether the value has been changed
      TaskMutator mutator = selectedTasks[i].createMutator();
      if (originalName == null || !originalName.equals(getTaskName())) {
        mutator.setName(getTaskName());
      }

      /*Function for resizing GanttBar*/
      if(BarField.getSelectedItem()=="LOW")
      {
    	  Barsize=5;

    	  }
      else if(BarField.getSelectedItem()=="MEDIUM")
      {
    	  Barsize=12;

      }
      else if(BarField.getSelectedItem()=="LARGE")
      {
    	  Barsize=18;
      }


      mutator.setProjectTask(false);
      if (originalWebLink == null || !originalWebLink.equals(getWebLink())) {
        mutator.setWebLink(getWebLink());
      }
      if (mileStoneCheckBox1 != null) {
        if (originalIsMilestone != isMilestone()) {
          mutator.setMilestone(isMilestone());
        }
      } else if (projectTaskCheckBox1 != null) {
        if (originalIsProjectTask != isProjectTask()) {
          mutator.setProjectTask(isProjectTask());
        }
      }
      if (!originalStartDate.equals(getStart())) {
        mutator.setStart(getStart());
      }
      if (!originalEndDate.equals(getEnd())) {
        mutator.setEnd(getEnd());
      }
      if (originalThirdDate == null && getThird() != null || originalThirdDate != null && getThird() == null
          || originalThirdDate != null && !originalThirdDate.equals(getThird())
          || originalThirdDateConstraint != getThirdDateConstraint()) {
        mutator.setThird(getThird(), getThirdDateConstraint());
      }

      if (getLength() > 0) {
        mutator.setDuration(selectedTasks[i].getManager().createLength(getLength()));
      }
      if (!Objects.equal(originalNotes, getNotes())) {
        mutator.setNotes(getNotes());
      }
      if (originalCompletionPercentage != getPercentComplete()) {
        mutator.setCompletionPercentage(getPercentComplete());
      }
      if (this.originalPriority != getPriority()) {
        mutator.setPriority(getPriority());
      }
     mutator.setColor(myTaskColorOption.getValue());
      if (this.originalShape == null && shapeComboBox.getSelectedIndex() != 0 || originalShape != null
          && !this.originalShape.equals(shapeComboBox.getSelectedPaint())) {
        mutator.setShape(new ShapePaint((ShapePaint) shapeComboBox.getSelectedPaint(), Color.white,
            myTaskColorOption.getValue()));
      }

      mutator.commit();
      myDependenciesPanel.commit();
      myAllocationsPanel.commit();

      if (!myShowInTimeline.isSelected()) {
        myUIfacade.getCurrentTaskView().getTimelineTasks().remove(selectedTasks[i]);
      } else {
        myUIfacade.getCurrentTaskView().getTimelineTasks().add(selectedTasks[i]);
      }
    }
  }

  private void setSelectedTaskProperties() {
    myUnpluggedClone = selectedTasks[0].unpluggedClone();
    nameField1.setText(originalName);

    setName(selectedTasks[0].toString());

    percentCompleteSlider.setValue(new Integer(originalCompletionPercentage));
    priorityComboBox.setSelectedIndex(originalPriority.ordinal());

    myTaskScheduleDates.setUnpluggedClone(myUnpluggedClone);
    if (originalThirdDate != null) {
      setThird(originalThirdDate.clone(), true);
    }
    thirdDateComboBox.setSelectedIndex(originalThirdDateConstraint);

    if (mileStoneCheckBox1 != null) {
      mileStoneCheckBox1.setSelected(originalIsMilestone);
    } else if (projectTaskCheckBox1 != null) {
      projectTaskCheckBox1.setSelected(originalIsProjectTask);
    }
    myTaskScheduleDates.setMilestone(isMilestone());

    tfWebLink.setText(originalWebLink);

    if (selectedTasks[0].shapeDefined()) {
      for (int j = 0; j < ShapeConstants.PATTERN_LIST.length; j++) {
        if (originalShape.equals(ShapeConstants.PATTERN_LIST[j])) {
          shapeComboBox.setSelectedIndex(j);
          break;
        }
      }
    }

    noteAreaNotes.setText(originalNotes);
    myTaskColorOption.setValue(selectedTasks[0].getColor());
    myShowInTimeline.setSelected(myUIfacade.getCurrentTaskView().getTimelineTasks().contains(selectedTasks[0]));
  }


  private boolean isMilestone() {
    if (mileStoneCheckBox1 == null) {
      return false;
    }
    return mileStoneCheckBox1.isSelected();
  }

  private boolean isProjectTask() {
    return projectTaskCheckBox1.isSelected();
  }

  private int getThirdDateConstraint() {
    return thirdDateComboBox.getSelectedIndex();
  }

  private String getNotes() {
    return noteAreaNotes.getText();
  }

  private String getTaskName() {
    String text = nameField1.getText();
    return text == null ? "" : text.trim();
  }

  private String getWebLink() {
    String text = tfWebLink.getText();
    return text == null ? "" : text.trim();
  }

  private int getPercentComplete() {
    return ((Integer) percentCompleteSlider.getValue()).hashCode();
  }

  private Task.Priority getPriority() {
    return Task.Priority.getPriority(priorityComboBox.getSelectedIndex());
  }

  private GanttCalendar getStart() {
    return myTaskScheduleDates.getStart();
  }

  private GanttCalendar getEnd() {
    return myTaskScheduleDates.getEnd();
  }

  private int getLength() {
    return myTaskScheduleDates.getLength();
  }

  private GanttCalendar getThird() {
    return myThird;
  }



  private void setThird(GanttCalendar third, @SuppressWarnings("unused") boolean test) {
    myThird = third;
    myThirdDatePicker.setDate(myThird.getTime());
  }

  private void storeOriginalValues(GanttTask task) {
    originalName = task.getName();
    originalWebLink = task.getWebLink();
    originalIsMilestone = task.isLegacyMilestone();
    originalStartDate = task.getStart();
    originalEndDate = task.getEnd();
    originalNotes = task.getNotes();
    originalCompletionPercentage = task.getCompletionPercentage();
    originalPriority = task.getPriority();
    originalShape = task.getShape();
    originalThirdDate = task.getThird();
    originalThirdDateConstraint = task.getThirdDateConstraint();
    originalIsProjectTask = task.isProjectTask();
  }

  private boolean canBeProjectTask(Task testedTask, TaskContainmentHierarchyFacade taskHierarchy) {
    Task[] nestedTasks = taskHierarchy.getNestedTasks(testedTask);
    if (nestedTasks.length == 0) {
      return false;
    }
    for (Task parent = taskHierarchy.getContainer(testedTask); parent != null; parent = taskHierarchy.getContainer(parent)) {
      if (parent.isProjectTask()) {
        return false;
      }
    }
    for (Task nestedTask : nestedTasks) {
      if (isProjectTaskOrContainsProjectTask(nestedTask)) {
        return false;
      }
    }
    return true;
  }

  private boolean isProjectTaskOrContainsProjectTask(Task task) {
    if (task.isProjectTask()) {
      return true;
    }
    for (Task nestedTask : task.getNestedTasks()) {
      if (isProjectTaskOrContainsProjectTask(nestedTask)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a milestone, a project task or no checkbox depending on the
   * selected task
   *
   * @return the created checkbox or null
   */
  private Pair<String, JCheckBox> constructCheckBox() {
    boolean canBeProjectTask = true;
    boolean canBeMilestone = true;
    TaskContainmentHierarchyFacade taskHierarchy = myTaskManager.getTaskHierarchy();
    for (Task task : selectedTasks) {
      canBeMilestone &= !taskHierarchy.hasNestedTasks(task);
      canBeProjectTask &= canBeProjectTask(task, taskHierarchy);
    }
    assert false == (canBeProjectTask && canBeMilestone);

    final Pair<String, JCheckBox> result;
    if (canBeProjectTask) {
      projectTaskCheckBox1 = new JCheckBox();
      result = Pair.create(language.getText("projectTask"), projectTaskCheckBox1);
    } else if (canBeMilestone) {
      mileStoneCheckBox1 = new JCheckBox(new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
          myTaskScheduleDates.setMilestone(isMilestone());
        }
      });
      result = Pair.create(language.getText("meetingPoint"), mileStoneCheckBox1);
    } else {
      result = null;
    }
    return result;
  }
}
