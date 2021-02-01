package com.progbits.web;

/**
 * Process a URL into pieces and allow processing at each step.
 *
 * @author scarr
 */
public class UrlEntry {

    private String currEntry = null;
    private String currUrl = null;

    public String getCurrEntry() {
        return currEntry;
    }

    public void setCurrEntry(String currEntry) {
        this.currEntry = currEntry;
    }

    public String getCurrUrl() {
        return currUrl;
    }

    public void setCurrUrl(String currUrl) {
        this.currUrl = currUrl;
    }

    public boolean chompUrl() {
        if (currUrl.startsWith("/")) {
            int iLoc = currUrl.indexOf("/", 1);

            if (iLoc == -1) {
                // We are at the end of the urls
                currEntry = currUrl;
                return false;
            } else {
                currEntry = currUrl.substring(0, iLoc);

                currUrl = currUrl.substring(iLoc);

                return true;
            }
        } else {
            return false;
        }
    }
}
