package ca.cgta.input.val.client.validator;

import java.util.List;

import ca.cgta.input.val.client.BaseLayoutPanel;
import ca.cgta.input.val.client.Messages;
import ca.cgta.input.val.shared.results.IStructure;
import ca.cgta.input.val.shared.results.ParsedBaseType;
import ca.cgta.input.val.shared.results.ParsedComponent;
import ca.cgta.input.val.shared.results.ParsedFailure;
import ca.cgta.input.val.shared.results.ParsedField;
import ca.cgta.input.val.shared.results.ParsedGroup;
import ca.cgta.input.val.shared.results.ParsedMessage;
import ca.cgta.input.val.shared.results.ParsedSegment;
import ca.cgta.input.val.shared.results.ParsedSubComponent;
import ca.cgta.input.val.shared.results.ValidationResult;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ScrollPanel;

public class ValidationResultsPanel extends BaseLayoutPanel {
	private final Messages messages = GWT.create(Messages.class);
	private FlowPanel myFailuresPanel;
	private ParsedFailure myHighlitedError = null;
	private HTML myParsedMessageHtml;

	private ValidationResult myResult;


	public ValidationResultsPanel(ValidationResult theResult) {
		super();

		myResult = theResult;
		boolean passed = myResult.getFailures().isEmpty();

		FlexTable northGrid = new FlexTable();
		addNorth(northGrid, 40);

		HTML lblNewLabel = new HTML(passed ? messages.validationPassed() : messages.validationFailed());
		northGrid.setWidget(0, 0, lblNewLabel);
		northGrid.getFlexCellFormatter().setWidth(0, 0, "400px");

		HTML lblMessageHeader = new HTML("<h1>Your Message</h1>");
		northGrid.setWidget(0, 1, lblMessageHeader);
		northGrid.getFlexCellFormatter().setWidth(0, 0, "400px");

		myFailuresPanel = new FlowPanel();
		addWest(new ScrollPanel(myFailuresPanel), 400);

		myParsedMessageHtml = new HTML();
		myParsedMessageHtml.addStyleName("parsedMessage");
		ScrollPanel parsedMessageScrollPanel = new ScrollPanel(myParsedMessageHtml);
		parsedMessageScrollPanel.setAlwaysShowScrollBars(true);
		add(parsedMessageScrollPanel);

		updateParsedMessage();
		updateFailuresList();

	}


	private static void addParsedMessage(HTML theContainer, ParsedMessage theMessage, ParsedGroup theParsedMessage, ParsedFailure theHighlitedError) {

		// Draw
		for (List<IStructure> nextList : theParsedMessage.getChildren()) {
			for (IStructure nextStructure : nextList) {
				if (nextStructure instanceof ParsedGroup) {
					addParsedMessage(theContainer, theMessage, (ParsedGroup) nextStructure, theHighlitedError);
				} else {
					addParsedSegment(theContainer, theMessage, (ParsedSegment) nextStructure, theHighlitedError);
				}
			}
		}
	}


	private static void addParsedSegment(HTML theContainer, ParsedMessage theMessage, ParsedSegment theSegment, ParsedFailure theHighlitedError) {
		// parsedTextNotHighlited

		Element element = theContainer.getElement();
		Document document = Document.get();
		if (element.hasChildNodes()) {
			element.appendChild(document.createBRElement());
		}

		SpanElement segmentSpan = document.createSpanElement();
		segmentSpan.setClassName("parsedSegment");
		element.appendChild(segmentSpan);

		boolean highlightSegment = false;
		if (theHighlitedError != null && theHighlitedError.getTerserPath().equals(theSegment.getTerserPath())) {
			segmentSpan.addClassName("parsedHighlitedError");
			highlightSegment = true;
		}

		// Segment Name
		SpanElement segmentNameSpan = document.createSpanElement();
		if (theHighlitedError != null) {
			if (highlightSegment) {
				segmentNameSpan.setClassName("parsedSegmentName");
				segmentNameSpan.addClassName("parsedHighlitedError");
			} else {
				segmentNameSpan.setClassName("parsedSegmentNameNotHighlited");
			}
		} else {
			segmentNameSpan.setClassName("parsedSegmentName");
		}

		segmentNameSpan.setInnerText(theSegment.getName());
		segmentSpan.appendChild(segmentNameSpan);

		int fieldIndex = 0;
		for (List<ParsedField> nextRepetitions : theSegment.getFieldRepetitions()) {

			// |
			SpanElement fieldDelimiter = document.createSpanElement();
			fieldDelimiter.setClassName("parsedDelimiter");
			fieldDelimiter.setInnerText(theMessage.getFieldSeparator());
			segmentSpan.appendChild(fieldDelimiter);

			int repIndex = 0;
			for (ParsedField nextRepetition : nextRepetitions) {

				String fieldTerserPath = theSegment.getTerserPath() + "-" + (fieldIndex + 1) + ((repIndex > 0) ? ("(" + (repIndex + 1) + ")") : "");
				boolean highlightField = theHighlitedError != null && theHighlitedError.getTerserPath().equals(fieldTerserPath);

				SpanElement fieldRepSpan = document.createSpanElement();
				segmentSpan.appendChild(fieldRepSpan);
				if (highlightField) {
					fieldRepSpan.addClassName("parsedHighlitedError");
				}

				// ~
				if (repIndex > 0) {
					SpanElement repDelimiter = document.createSpanElement();
					repDelimiter.setClassName("parsedDelimiter");
					repDelimiter.setInnerText("" + theMessage.getRepetitionSeparator());
					fieldRepSpan.appendChild(repDelimiter);

					if (highlightField) {
						repDelimiter.addClassName("parsedHighlitedError");
					}

				}

				if (nextRepetition.getValueIfLeaf() != null) {
					if (!"MSH".equals(theSegment.getTerserPath()) || fieldIndex > 0) {
						addParsedText(fieldRepSpan, nextRepetition, highlightField || highlightSegment, highlightField, theHighlitedError);
					}
				} else {

					// Components
					int componentIndex = 1;
					for (ParsedComponent nextComponent : nextRepetition.getChildren()) {

						String cmpTerserPath = fieldTerserPath + "-" + componentIndex;
						boolean highlightCmp = theHighlitedError != null && theHighlitedError.getTerserPath().equals(cmpTerserPath);

						// ^
						if (componentIndex > 1) {
							SpanElement compDelimiter = document.createSpanElement();
							compDelimiter.setClassName("parsedDelimiter");
							compDelimiter.setInnerText("" + theMessage.getComponentSeparator());
							fieldRepSpan.appendChild(compDelimiter);
						}

						if (nextComponent.getValueIfLeaf() != null) {
							addParsedText(fieldRepSpan, nextComponent, highlightField || highlightSegment || highlightCmp, highlightCmp, theHighlitedError);
						} else {

							int subCmpIndex = 1;
							for (ParsedSubComponent nextSubComponent : nextComponent.getChildren()) {

								String subCmpTerserPath = cmpTerserPath + "-" + componentIndex;
								boolean highlightSubCmp = theHighlitedError != null && theHighlitedError.getTerserPath().equals(subCmpTerserPath);

								// &
								if (subCmpIndex > 1) {
									SpanElement compDelimiter = document.createSpanElement();
									compDelimiter.setClassName("parsedDelimiter");
									compDelimiter.setInnerText("" + theMessage.getSubComponentSeparator());
									fieldRepSpan.appendChild(compDelimiter);
								}

								addParsedText(fieldRepSpan, nextSubComponent, highlightField || highlightSegment || highlightCmp || highlightSubCmp, highlightSubCmp, theHighlitedError);

							}

						}

						componentIndex++;
					}

				}

				repIndex++;
			}

			fieldIndex++;
		}

	}


	/**
	 * 
	 * @param theSegmentSpan
	 * @param theNextRepetition
	 * @param theWithinHighlight
	 * @param theHighlightSpecificStructure
	 *            Set to true if this specific structure is highlighted. I.e. if
	 *            this is a component, and that exact component's terser path is
	 *            listed as highlited
	 * @param theHighlitedError 
	 */
	private static void addParsedText(SpanElement theSegmentSpan, ParsedBaseType<?> theNextRepetition, boolean theWithinHighlight, boolean theHighlightSpecificStructure, ParsedFailure theHighlitedError) {
		if (theHighlitedError == null) {
			theSegmentSpan.appendChild(Document.get().createTextNode(theNextRepetition.getValueIfLeaf()));
		} else if (theWithinHighlight) {
			SpanElement spanElement = Document.get().createSpanElement();
			spanElement.setInnerText(theNextRepetition.getValueIfLeaf());
			spanElement.addClassName("parsedHighlitedError");
			
			if (theHighlightSpecificStructure && theNextRepetition.getValueIfLeaf().trim().length() == 0) {
				spanElement.setInnerHTML("&nbsp;");
			}
			
			theSegmentSpan.appendChild(spanElement);
		} else {
			SpanElement spanElement = Document.get().createSpanElement();
			spanElement.setInnerText(theNextRepetition.getValueIfLeaf());
			spanElement.addClassName("parsedTextNotHighlited");
			theSegmentSpan.appendChild(spanElement);
		}
	}


	private void updateFailuresList() {
		myFailuresPanel.clear();

		Button anotherBtn = new Button("&lt; Validate Another");
		anotherBtn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent theEvent) {
				History.newItem("", true);
			}
		});
		myFailuresPanel.add(anotherBtn);
		
		if (myResult.getFailures().isEmpty()) {
			return;
		}
		
		myFailuresPanel.add(new HTML(messages.failuresHeader()));

		int issueIndex = 1;
		for (final ParsedFailure next : myResult.getFailures()) {

			HTML header = new HTML("Issue " + issueIndex);
			header.addStyleName("issueNumber");
			myFailuresPanel.add(header);

			final FlexTable issueContainer = new FlexTable();
			issueContainer.addStyleName("issueContainer");
			myFailuresPanel.add(issueContainer);

			PushButton highlightButton = new PushButton("Highlight");
			
			if (next == myHighlitedError) {
				highlightButton.setText("Showing");
				highlightButton.setEnabled(false);
			}
			
			issueContainer.setWidget(0, 0, highlightButton);
			issueContainer.getFlexCellFormatter().setRowSpan(0, 0, 5);
			issueContainer.getFlexCellFormatter().setVerticalAlignment(0, 0, HasVerticalAlignment.ALIGN_TOP);
			issueContainer.setWidget(0, 1, new HTML("<b>Location Within Message</b>: " + next.getTerserPath()));
			issueContainer.setWidget(1, 0, new HTML("<b>Error Code</b>: " + next.getFailureCode()));
			issueContainer.setWidget(2, 0, new HTML("<b>Description</b>: " + next.getMessage()));
			issueContainer.setWidget(3, 0, new HTML("<b>Severity</b>: " + next.getSeverityDescription()));

			if (next.getStepsToResolve() != null && next.getStepsToResolve().length() > 0) {
				issueContainer.setWidget(4, 0, new HTML("<b>Steps to Resolve</b>: " + next.getStepsToResolve()));
			}
			
			highlightButton.addClickHandler(new ClickHandler() {
				public void onClick(ClickEvent theEvent) {
					myHighlitedError = next;
					updateParsedMessage();
					updateFailuresList();
				}
			});

			issueIndex++;
		}
	}


	private void updateParsedMessage() {
		// Clear anything existing
		NodeList<Node> childNodes = myParsedMessageHtml.getElement().getChildNodes();
		while (childNodes.getLength() > 0) {
			myParsedMessageHtml.getElement().removeChild(childNodes.getItem(0));
		}

		addParsedMessage(myParsedMessageHtml, myResult.getParsedMessage(), myResult.getParsedMessage(), myHighlitedError);
	}

}
