package com.eoscode.springapitools.data.repository;

import com.eoscode.springapitools.data.domain.NoDelete;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;

@Repository
public class DefaultDeleteByIdRepository<Entity, ID> extends BaseRepository<Entity, ID>
        implements CustomDeleteByIdRepository<Entity, ID> {

    @Override
    public int deleteById(Class<Entity> entityClass, ID id) {
        NoDelete deleteNoEntityById = entityClass.getAnnotation(NoDelete.class);
        Query query;
        if (deleteNoEntityById != null) {
            query = getEntityManger()
                    .createQuery(String.format("update %s as obj set obj.%s = %s where obj.id = :id",
                            entityClass.getSimpleName(), deleteNoEntityById.field(), deleteNoEntityById.deletedValue()));
        } else {
            query = getEntityManger()
                    .createQuery(String.format("delete %s as obj where obj.id = :id",
                            entityClass.getSimpleName()));
        }
        query.setParameter("id", id);
        return query.executeUpdate();
    }

}
