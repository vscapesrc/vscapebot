package scripts;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.vsbot.wrappers.RSInterface;
import com.vsbot.wrappers.RSInterfaceChild;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

@ScriptManifest(authors = { "joku.rules" }, name = "Interface Explorer", version = 0.3, description = "Fetches various interface data for developers.")
public class InterfaceExplorer extends Script {
	public RSInterfaceChildWrap uhmaIk = null;
	private class InterfaceTreeModel implements TreeModel {
		private final Object root = new Object();
		private final ArrayList<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();

		// only call getAllInterfaces() once per GUI update, because
		// otherwise closed interfaces might mess up the indexes
		private final ArrayList<RSInterfaceWrap> interfaceWraps = new ArrayList<RSInterfaceWrap>();

		@Override
		public void addTreeModelListener(final TreeModelListener l) {
			treeModelListeners.add(l);
		}

		private void fireTreeStructureChanged(final Object oldRoot) {
			treeModelListeners.size();
			final TreeModelEvent e = new TreeModelEvent(this,
					new Object[] { oldRoot });
			for (final TreeModelListener tml : treeModelListeners) {
				tml.treeStructureChanged(e);
			}
		}

		@Override
		public Object getChild(final Object parent, final int index) {
			if (parent == root) {
				return interfaceWraps.get(index);
			} else if (parent instanceof RSInterfaceWrap) {
				return new RSInterfaceChildWrap(
						((RSInterfaceWrap) parent).wrapped.getChildren()[index]);
			} else if (parent instanceof RSInterfaceChildWrap) {
				return 0;
			}
			return null;
		}

		@Override
		public int getChildCount(final Object parent) {
			if (parent == root) {
				return interfaceWraps.size();
			} else if (parent instanceof RSInterfaceWrap) {
				return ((RSInterfaceWrap) parent).wrapped.getChildren().length;
			} else if (parent instanceof RSInterfaceChildWrap) {
				return 0;
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(final Object parent, final Object child) {
			if (parent == root) {
				return interfaceWraps.indexOf(child);
			} else if (parent instanceof RSInterfaceWrap) {
				return Arrays.asList(
						((RSInterfaceWrap) parent).wrapped.getChildren())
						.indexOf(((RSInterfaceChildWrap) child).wrapped);
			} else if (parent instanceof RSInterfaceChildWrap) {
				return -1;
			}
			return -1;
		}

		@Override
		public Object getRoot() {
			return root;
		}

		@Override
		public boolean isLeaf(final Object o) {
			return o instanceof RSInterfaceChildWrap;
		}

		@Override
		public void removeTreeModelListener(final TreeModelListener l) {
			treeModelListeners.remove(l);
		}

		public boolean searchMatches(final RSInterfaceChild iface,
				final String contains) {
			return iface.getText().toLowerCase()
					.contains(contains.toLowerCase());
		}

		public void update(final String search) {
			interfaceWraps.clear();

			for (final RSInterface iface : interfaces.getAllParents()) {
				toBreak: for (final RSInterfaceChild child : iface
						.getChildren()) {
					if (searchMatches(child, search)) {
						interfaceWraps.add(new RSInterfaceWrap(iface));
						break;
					}

				}
			}
			fireTreeStructureChanged(root);
		}

		@Override
		public void valueForPathChanged(final TreePath path,
				final Object newValue) {
			// tree represented by this model isn't editable
		}
	}

	private class RSInterfaceChildWrap {
		public RSInterfaceChild wrapped;

		public RSInterfaceChildWrap(final RSInterfaceChild wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof RSInterfaceChildWrap
					&& wrapped == ((RSInterfaceChildWrap) o).wrapped;
		}

		/*
		 * @Override public String toString() { return "Component " +
		 * wrapped.getIndex(); }
		 */
	}

	// these wrappers just add toString() methods
	private class RSInterfaceWrap {
		public RSInterface wrapped;

		public RSInterfaceWrap(final RSInterface wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public boolean equals(final Object o) {
			return o instanceof RSInterfaceWrap
					&& wrapped == ((RSInterfaceWrap) o).wrapped;
		}

		/*
		 * @Override public String toString() { return "Interface " +
		 * wrapped.getIndex(); }
		 */
	}

	private JFrame window;
	private JTree tree;

	private InterfaceTreeModel treeModel;

	private JPanel infoArea;

	private JTextField searchBox;

	private Rectangle highlightArea = null;

	@Override
	public int loop() {
		if (window.isVisible()) {
			return 1000;
		}
		return -1;
	}

	@Override
	public void onBegin() {
		try {
			System.out.println("onbegin");
			window = new JFrame("Interface Explorer");
			window.setVisible(true);
			treeModel = new InterfaceTreeModel();
			treeModel.update("");
			tree = new JTree(treeModel);
			tree.setRootVisible(false);
			tree.setEditable(false);
			tree.getSelectionModel().setSelectionMode(
					TreeSelectionModel.SINGLE_TREE_SELECTION);
			tree.addTreeSelectionListener(new TreeSelectionListener() {
				private void addInfo(final String key, final String value) {
					final JPanel row = new JPanel();
					row.setAlignmentX(Component.LEFT_ALIGNMENT);
					row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

					for (final String data : new String[] { key, value }) {
						final JLabel label = new JLabel(data);
						label.setAlignmentY(Component.TOP_ALIGNMENT);
						row.add(label);
					}
					infoArea.add(row);
				}

				@Override
				public void valueChanged(final TreeSelectionEvent e) {
					final Object node = tree.getLastSelectedPathComponent();
					if (node == null || node instanceof RSInterfaceWrap) {
						return;
					}
					// at this point the node can only be an instace of
					// RSInterfaceChildWrap
					// or of RSInterfaceComponentWrap

					infoArea.removeAll();
					RSInterfaceChild iface = null;
					if (node instanceof RSInterfaceChildWrap) {
						uhmaIk = (RSInterfaceChildWrap) node;
						highlightArea = ((RSInterfaceChildWrap) node).wrapped
								.getArea();
						iface = ((RSInterfaceChildWrap) node).wrapped;
					}
					if (iface == null) {
						return;
					}
					addInfo("Action type: ", "-1" /* + iface.getActionType() */);
					addInfo("Type: ", "" + iface.getType());
					addInfo("SpecialType: ", "" + "0");
					addInfo("Bounds Index: ", "" + "some");
					addInfo("Model ID: ", "" + "some");
					addInfo("Texture ID: ", "" + "some");
					addInfo("Parent ID: ", "" + iface.getParent().getId());
					addInfo("Text: ", "" + iface.getText());
					addInfo("Tooltip: ", "" + "null");
					addInfo("SelActionName: ", "" + "null");
					if (iface.getActions() != null) {
						String actions = "";
						for (final String action : iface.getActions()) {
							if (!actions.equals("")) {
								actions += "\n";
							}
							actions += action;
						}
						addInfo("Actions: ", actions);
					}
					addInfo("Component ID: ", "" + iface.getId());
					addInfo("Component Stack Size: ", "" + 0);
					addInfo("Relative Location: ", "(" + iface.getScreenX()
							+ "," + iface.getScreenY() + ")");
					addInfo("Absolute Location: ", "(" + iface.getScreenX()
							+ "," + iface.getScreenY() + ")");

					infoArea.validate();
					infoArea.repaint();
				}
			});

			JScrollPane scrollPane = new JScrollPane(tree);
			scrollPane.setPreferredSize(new Dimension(250, 500));
			window.add(scrollPane, BorderLayout.WEST);

			infoArea = new JPanel();
			infoArea.setLayout(new BoxLayout(infoArea, BoxLayout.Y_AXIS));
			scrollPane = new JScrollPane(infoArea);
			scrollPane.setPreferredSize(new Dimension(250, 500));
			window.add(scrollPane, BorderLayout.CENTER);

			final ActionListener actionListener = new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					treeModel.update(searchBox.getText());
					infoArea.removeAll();
					infoArea.validate();
					infoArea.repaint();
				}
			};

			final JPanel toolArea = new JPanel();
			toolArea.setLayout(new FlowLayout(FlowLayout.LEFT));
			toolArea.add(new JLabel("Filter:"));

			searchBox = new JTextField(20);
			searchBox.addActionListener(actionListener);
			toolArea.add(searchBox);
			System.out.println("here");

			final JButton updateButton = new JButton("Update");
			updateButton.addActionListener(actionListener);
			toolArea.add(updateButton);
			window.add(toolArea, BorderLayout.NORTH);

			window.pack();
			window.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void paint(Graphics g) {
		if (highlightArea != null) {
			g.setColor(Color.ORANGE);
			g.drawRect(highlightArea.x, highlightArea.y, highlightArea.width,
					highlightArea.height);
			g.drawString(uhmaIk.wrapped.getAccessor().getMasterX() + " " + uhmaIk.wrapped.getAccessor().getMasterY(), 10, 10);
			g.drawString("Width: " + highlightArea.height + " width : " + highlightArea.width, 10, 30);
		}

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}
}
