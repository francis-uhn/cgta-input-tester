package ca.cgta.input.val.client;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.Widget;

/**
 * An HTML br
 */
public class HtmlBr extends Widget {

    public HtmlBr() {
        setElement(Document.get().createBRElement());
        getElement().setAttribute("clear", "all");
    }
    
}
