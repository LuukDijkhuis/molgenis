package org.molgenis.ontology.core.meta;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class OntologyTermDynamicAnnotationFactory
    extends AbstractSystemEntityFactory<
        OntologyTermDynamicAnnotation, OntologyTermDynamicAnnotationMetaData, String> {
  OntologyTermDynamicAnnotationFactory(
      OntologyTermDynamicAnnotationMetaData ontologyTermDynamicAnnotationMeta,
      EntityPopulator entityPopulator) {
    super(OntologyTermDynamicAnnotation.class, ontologyTermDynamicAnnotationMeta, entityPopulator);
  }
}
