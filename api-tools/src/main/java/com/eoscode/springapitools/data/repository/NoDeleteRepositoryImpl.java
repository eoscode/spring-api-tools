package com.eoscode.springapitools.data.repository;

import com.eoscode.springapitools.data.domain.NoDelete;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

@Repository
public class NoDeleteRepositoryImpl<Entity, ID> extends BaseCustomRepository implements NoDeleteRepositoryCustom<Entity, ID> {

    @Override
    public void noDeleteById(Class<Entity> entityClass, ID id) {
        NoDelete deleteNoEntityById = entityClass.getAnnotation(NoDelete.class);
        Query query = getEntityManger()
                .createQuery(String.format("update %s as obj set obj.%s = %s where obj.id = :id",
                        entityClass.getSimpleName(), deleteNoEntityById.field(), deleteNoEntityById.deleteValue()));
        query.setParameter("id", id);
        query.executeUpdate();
    }

}
