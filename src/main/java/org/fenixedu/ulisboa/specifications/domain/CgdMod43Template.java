package org.fenixedu.ulisboa.specifications.domain;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;

public class CgdMod43Template extends CgdMod43Template_Base {

    protected CgdMod43Template() {
        super();
    }

    public CgdMod43Template(final String filename, final byte[] content) {
        this();
        init(filename, filename, content);
    }

    @Override
    public boolean isAccessible(User user) {
        return Group.parse("#managers").isMember(user);
    }

}
