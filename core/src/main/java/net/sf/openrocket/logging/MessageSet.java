package net.sf.openrocket.logging;

import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Monitorable;
import net.sf.openrocket.util.Mutable;

import java.util.AbstractSet;
import java.util.Iterator;

/**
 * A set that contains multiple <code>Message</code>s.  When adding a
 * {@link Message} to this set, the contents is checked for a message of the
 * same type.  If one is found, then the message left in the set is determined
 * by the method {@link Message#replaceBy(Message)}.
 * <p>
 * A MessageSet can be made immutable by calling {@link #immute()}.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class MessageSet<E extends Message> extends AbstractSet<E> implements Cloneable, Monitorable {
    /** the actual array of messages */
    protected ArrayList<E> messages = new ArrayList<>();

    protected Mutable mutable = new Mutable();
    private int modID = 0;

    /**
     * Add a <code>Message</code> to the set.  If a message of the same type
     * exists in the set, the message that is left in the set is defined by the
     * method {@link Message#replaceBy(Message)}.
     *
     * @throws IllegalStateException	if this message set has been made immutable.
     */
    @Override
    public boolean add(E m) {
        mutable.check();

        modID++;
        int index = messages.indexOf(m);

        if (index < 0) {
            messages.add(m);
            return false;
        }

        E old = messages.get(index);
        if (old.replaceBy(m)) {
            messages.set(index, m);
        }

        return true;
    }

    /**
     * Add a <code>Message</code> with the specified text to the set.
     *
     * @param s		the message text.
     * @throws IllegalStateException	if this message set has been made immutable.
     */
    public abstract boolean add(String s);

    /**
     * Add a <code>Message</code> of the specified type with the specified discriminator to the
     * set.
     * @param m the message
     * @param d the extra discriminator
     *
     */
    public boolean add (E m, String d) {
        return this.add(m.toString() + ":  \"" + d + "\"");
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> iterator = messages.iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                mutable.check();
                iterator.remove();
            }
        };
    }

    @Override
    public int size() {
        return messages.size();
    }


    public void immute() {
        mutable.immute();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (Message m : messages) {
            if (s.length() > 0)
                s.append(",");
            s.append(m.toString());
        }
        return "Messages[" + s + "]";
    }

    @Override
    public int getModID() {
        return modID;
    }
}
