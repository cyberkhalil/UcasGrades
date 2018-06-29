package UcasGrades;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.IOException;

/**
 * class for getting the information about marks from UCAS website
 *
 * @author khalil2535
 */
class UcasWeb {

    private static final WebClient webClient = new WebClient();

    static Authorization getAuthorized(String username, String password) {
        try {
            final HtmlPage page1 = webClient.getPage("https://my.ucas.edu.ps/login?backurl=/home");

            final HtmlForm form = (HtmlForm) page1.getElementById("form1");
            HtmlAnchor link = null;

            for (HtmlElement x : form.getElementsByTagName("a")) {
                HtmlAnchor tempLink = ((HtmlAnchor) x);
                if ("lnkLogin".equals(tempLink.getId())) {
                    link = tempLink;
                    break;
                }
            }
            final HtmlTextInput textField = form.getInputByName("txtUsername");
            textField.setValueAttribute(username);
            final HtmlPasswordInput textField2 = form.getInputByName("txtPassword");
            textField2.setValueAttribute(password);

            if (link != null) {
                link.click();
            }
            HtmlForm tempForm = ((HtmlForm) ((HtmlPage) webClient
                    .getPage("https://my.ucas.edu.ps/Registration/transcript")).getElementById("form1"));
            return (tempForm.getInputsByName("txtUsername").isEmpty()) ? Authorization.ACCEPTED : Authorization.DENIED;
        } catch (IOException e) {
            Utilities.printException("Didn't get Authorized", e);
            return Authorization.NOT_FOUND;
        }
    }

    static String getMarksPage() {
        try {
            return webClient.getPage("https://my.ucas.edu.ps/Registration/transcript").getWebResponse()
                    .getContentAsString();
        } catch (IOException e) {
            Utilities.printException("Exception in getMarksPage method", e);
        }
        return "ф Error in getting marks page ф";
    }

}
