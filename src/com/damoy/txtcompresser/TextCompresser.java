package com.damoy.txtcompresser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;

public class TextCompresser extends JFrame {

	private static final long serialVersionUID = 7840185264770309196L;
	
	private JTextArea textArea;
	private LimitedRowLengthDocument document;
	private JButton launchButton;
	private JButton openButton;

	private TextCompresser() {
		super("TextCompresser");
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		int width = 480;
		int height = 320;
		
		// Text area
		textArea = new JTextArea();
		Font f = new Font("Tahoma", Font.PLAIN, 11);
		textArea.setFont(f);
		document = new LimitedRowLengthDocument(textArea, computeMaxHorizontalLength(width));
		textArea.setDocument(document);

		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setTabSize(1);
		textArea.setEditable(true);

		// frame configuration
		BorderLayout bl = new BorderLayout();
		this.setLayout(bl);

		this.setMinimumSize(new Dimension(width, height));
		this.setPreferredSize(new Dimension(width, height));
		this.setLocationRelativeTo(null);

		this.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				Component c = (Component) evt.getSource();
				document.setMax(computeMaxHorizontalLength(c.getWidth()));
			}
		});

		this.launchButton = generateLaunchButton();
		this.openButton = generateOpenFileButton();

		setFocusable(true);
		requestFocus();
		add(launchButton, BorderLayout.SOUTH);
		add(openButton, BorderLayout.EAST);
		add(new JScrollPane(textArea), BorderLayout.CENTER);
		pack();
		setVisible(true);
	}
	
	private JButton generateLaunchButton() {
		JButton button = new JButton("Compress");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				compressAction(TextCompresser.this.textArea.getText());
			}
		});
		
		return button;
	}
	
	private void compressAction(String text) {
		final JFrame frame = new JFrame("Compression result");
		frame.setMinimumSize(TextCompresser.this.getSize());
		frame.setPreferredSize(TextCompresser.this.getSize());
		frame.setLocationRelativeTo(null);

		final JTextArea result = new JTextArea(compress(text));
		JButton copyButton = new JButton("Copy");
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Toolkit.getDefaultToolkit().getSystemClipboard()
						.setContents(new StringSelection(result.getText()), null);
			}
		});

		frame.add(new JScrollPane(result), BorderLayout.CENTER);
		frame.add(copyButton, BorderLayout.SOUTH);

		result.setEditable(false);
		Font f = new Font("Tahoma", Font.PLAIN, 11);
		result.setFont(f);

		result.setLineWrap(true);
		result.setWrapStyleWord(true);

		frame.add(result);
		frame.pack();
		frame.setVisible(true);
	}
	
	private JButton generateOpenFileButton() {
		JButton button = new JButton("Load");
		
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				int state = chooser.showOpenDialog(null);
				
				if(state == JFileChooser.APPROVE_OPTION) {
					try {
						Scanner scanner = new Scanner(new File(chooser.getSelectedFile().getAbsolutePath()));
						StringBuilder sb = new StringBuilder();
						
						while(scanner.hasNextLine()){
							sb.append(scanner.nextLine());
							sb.append("\n");
						}
						
						scanner.close();
						TextCompresser.this.textArea.setText(sb.toString());
					} catch (FileNotFoundException e1) {
						System.out.println("File not found.");
					}
				}
			}
		});
		
		return button;
	}

	private String compress(String text) {
		StringBuilder sBuilder = new StringBuilder();
		String[] lines = text.split("\n");

		for (int i = 0; i < lines.length; ++i) {
			sBuilder.append(lines[i].trim());
		}

		text = sBuilder.toString();
		return text.trim().replaceAll("[\t\b\r]", "");
	}

	private int computeMaxHorizontalLength(int width) {
		return width / 8;
	}

	public static void main(String[] args) {
		new TextCompresser();
	}

	public class LimitedRowLengthDocument extends DefaultStyledDocument {

		private static final long serialVersionUID = -2059737037274062490L;
		private static final String EOL = "\n";

		private int max;
		private JTextArea textArea = null;

		public LimitedRowLengthDocument(JTextArea ta, int max) {
			this.textArea = ta;
			this.max = max;
		}

		public void insertString(int offs, String str, AttributeSet attribute) throws BadLocationException {
			int actRow = textArea.getLineOfOffset(offs);
			int rowBeginn = textArea.getLineStartOffset(actRow);
			int rowEnd = textArea.getLineEndOffset(actRow);
			int referenceValue = 0;

			if (str.length() > 1) {
				referenceValue = (rowEnd + str.length()) - rowBeginn;
			} else {
				referenceValue = rowEnd - rowBeginn;
			}

			if (referenceValue >= max) {
				if (str.length() > 1) {
					StringBuffer str_buff = new StringBuffer();
					for (int i = 0; i < str.length(); i++) {
						if (i >= max) {
							str_buff.append(EOL);
							str_buff.append(str.charAt(i));
							str = str.substring(i, str.length());
							i = 0;
						} else {
							str_buff.append(str.charAt(i));
						}
					}
					str = str_buff + EOL;
				} else {
					str = EOL + str;
				}
			}

			super.insertString(offs, str, attribute);
		}

		public void setMax(int max) {
			this.max = max;
		}

		public int getLimit() {
			return max;
		}
	}

}
