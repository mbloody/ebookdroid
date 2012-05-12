package org.ebookdroid.opds;

import java.util.List;

public class Book extends Entry {

    public Author author;
    public Link thumbnail;
    public List<Link> downloads;

    public Book(final Feed parent, final String id, final String title, final Content content) {
        super(parent, id, title, content);
    }

}
