package org.molgenis.data;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.data.meta.AttributeType.MREF;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.AttributeType.XREF;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class CascadeDeleteRepositoryDecoratorTest {
  private static final String XREF_ATTR_NAME = "xrefAttr";
  private static final String REF_ENTITY_TYPE_NAME = "refEntityType";
  private static final Object REF_ENTITY_ID = "REF_ENTITY_ID";

  @Mock private Repository<Entity> delegateRepository;

  @Mock private DataService dataService;

  private CascadeDeleteRepositoryDecorator cascadeDeleteRepositoryDecorator;

  @Mock private EntityType entityType;
  @Mock private Attribute stringAttr;
  @Mock private Attribute xrefAttr;
  @Mock private EntityType refEntityType;
  @Mock private Entity entity;
  @Mock private Entity refEntity;

  @BeforeMethod
  public void setUpBeforeMethod() {
    initMocks(this);
    cascadeDeleteRepositoryDecorator =
        new CascadeDeleteRepositoryDecorator(delegateRepository, dataService);

    when(refEntityType.getId()).thenReturn(REF_ENTITY_TYPE_NAME);
    when(refEntityType.getAtomicAttributes()).thenReturn(emptyList());

    when(refEntity.getIdValue()).thenReturn(REF_ENTITY_ID);
    when(stringAttr.getName()).thenReturn("stringAttr");
    when(stringAttr.getDataType()).thenReturn(STRING);
    when(xrefAttr.getName()).thenReturn(XREF_ATTR_NAME);
    when(xrefAttr.getDataType()).thenReturn(XREF);
    when(xrefAttr.getRefEntity()).thenReturn(refEntityType);

    when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, xrefAttr));
    when(delegateRepository.getEntityType()).thenReturn(entityType);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testDelegate() {
    new CascadeDeleteRepositoryDecorator(null, null);
  }

  @Test
  public void testDeleteNoCascade() {
    when(xrefAttr.getCascadeDelete()).thenReturn(null);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteCascadeNotNull() {
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteCascadeNull() {
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(null);
    cascadeDeleteRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteCascadeMrefEmpty() {
    String mrefAttrName = "mrefAttrName";
    Attribute mrefAttr = mock(Attribute.class);
    when(mrefAttr.getName()).thenReturn(mrefAttrName);
    when(mrefAttr.getDataType()).thenReturn(MREF);
    when(mrefAttr.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, mrefAttr));

    when(mrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntities(mrefAttrName)).thenReturn(emptyList());
    cascadeDeleteRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteCascadeMrefNotEmpty() {
    String mrefAttrName = "mrefAttrName";
    Attribute mrefAttr = mock(Attribute.class);
    when(mrefAttr.getName()).thenReturn(mrefAttrName);
    when(mrefAttr.getDataType()).thenReturn(MREF);
    when(mrefAttr.getRefEntity()).thenReturn(refEntityType);
    when(entityType.getAtomicAttributes()).thenReturn(asList(stringAttr, mrefAttr));
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    when(mrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntities(mrefAttrName)).thenReturn(singletonList(refEntity));
    cascadeDeleteRepositoryDecorator.delete(entity);
    verify(delegateRepository).delete(entity);
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteByIdCascadeNotNull() {
    String entityId = "id";
    when(delegateRepository.findOneById(entityId)).thenReturn(entity);
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.deleteById(entityId);
    verify(delegateRepository).deleteById(entityId);
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteByIdNoCascade() {
    String entityId = "id";
    when(delegateRepository.findOneById(entityId)).thenReturn(entity);
    when(xrefAttr.getCascadeDelete()).thenReturn(null);

    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.deleteById(entityId);
    verify(delegateRepository).deleteById(entityId);
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAllCascadeNotNull() {
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);

    doAnswer(
            invocation -> {
              Consumer<List<Entity>> consumer = invocation.getArgument(0);
              consumer.accept(singletonList(entity));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), eq(1000));

    cascadeDeleteRepositoryDecorator.deleteAll();
    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(entity));
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @Test
  public void testDeleteAllNoCascade() {
    when(xrefAttr.getCascadeDelete()).thenReturn(null);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteStreamCascadeNotNull() {
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.delete(Stream.of(entity));
    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(entity));
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteStreamNoCascade() {
    when(xrefAttr.getCascadeDelete()).thenReturn(null);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.delete(Stream.of(entity));
    ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(entity));
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAllStreamCascadeNotNull() {
    String entityId = "id";
    when(delegateRepository.findAll(any(Stream.class))).thenReturn(Stream.of(entity));
    when(xrefAttr.getCascadeDelete()).thenReturn(Boolean.TRUE);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.deleteAll(Stream.of(entityId));
    ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(entityId));
    verify(dataService).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testDeleteAllStreamNoCascade() {
    String entityId = "id";
    when(delegateRepository.findAll(any(Stream.class))).thenReturn(Stream.of(entity));
    when(xrefAttr.getCascadeDelete()).thenReturn(null);
    when(entity.getEntity(XREF_ATTR_NAME)).thenReturn(refEntity);
    when(dataService.findOneById(REF_ENTITY_TYPE_NAME, REF_ENTITY_ID)).thenReturn(refEntity);
    cascadeDeleteRepositoryDecorator.deleteAll(Stream.of(entityId));
    ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(captor.capture());
    assertEquals(captor.getValue().collect(toList()), singletonList(entityId));
    verify(dataService, never()).delete(REF_ENTITY_TYPE_NAME, refEntity);
  }
}
