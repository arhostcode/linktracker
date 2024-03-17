/*
 * This file is generated by jOOQ.
 */
package edu.java.domain.jooq;


import edu.java.domain.jooq.tables.ChatLink;
import edu.java.domain.jooq.tables.Databasechangelog;
import edu.java.domain.jooq.tables.Databasechangeloglock;
import edu.java.domain.jooq.tables.Link;
import edu.java.domain.jooq.tables.TgChat;

import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.Generated;

import org.jetbrains.annotations.NotNull;
import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "https://www.jooq.org",
        "jOOQ version:3.18.9"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes", "this-escape" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.chat_link</code>.
     */
    public final ChatLink CHAT_LINK = ChatLink.CHAT_LINK;

    /**
     * The table <code>public.databasechangelog</code>.
     */
    public final Databasechangelog DATABASECHANGELOG = Databasechangelog.DATABASECHANGELOG;

    /**
     * The table <code>public.databasechangeloglock</code>.
     */
    public final Databasechangeloglock DATABASECHANGELOGLOCK = Databasechangeloglock.DATABASECHANGELOGLOCK;

    /**
     * The table <code>public.link</code>.
     */
    public final Link LINK = Link.LINK;

    /**
     * The table <code>public.tg_chat</code>.
     */
    public final TgChat TG_CHAT = TgChat.TG_CHAT;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    @Override
    @NotNull
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    @NotNull
    public final List<Table<?>> getTables() {
        return Arrays.asList(
            ChatLink.CHAT_LINK,
            Databasechangelog.DATABASECHANGELOG,
            Databasechangeloglock.DATABASECHANGELOGLOCK,
            Link.LINK,
            TgChat.TG_CHAT
        );
    }
}
