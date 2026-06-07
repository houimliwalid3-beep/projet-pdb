package org.isfce.pdb.dao;

import java.util.List;
import org.isfce.pdb.model.Installation;

public interface IInstallationDao extends IDAO<Installation, Integer> {
    List<Installation> getListe();
}