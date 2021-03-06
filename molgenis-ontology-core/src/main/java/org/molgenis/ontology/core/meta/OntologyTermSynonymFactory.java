package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermSynonymFactory
    extends AbstractSystemEntityFactory<OntologyTermSynonym, OntologyTermSynonymMetaData, String> {
  OntologyTermSynonymFactory(
      OntologyTermSynonymMetaData ontologyTermSynonymMetaData, EntityPopulator entityPopulator) {
    super(OntologyTermSynonym.class, ontologyTermSynonymMetaData, entityPopulator);
  }
}
