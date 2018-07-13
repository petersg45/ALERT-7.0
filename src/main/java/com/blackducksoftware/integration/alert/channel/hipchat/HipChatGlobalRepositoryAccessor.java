package com.blackducksoftware.integration.alert.channel.hipchat;

import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.channel.hipchat.model.HipChatGlobalRepository;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;

public class HipChatGlobalRepositoryAccessor extends RepositoryAccessor {
    private final HipChatGlobalRepository repository;

    public HipChatGlobalRepositoryAccessor(final HipChatGlobalRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @Override
    public DatabaseEntity saveDistributionEntity(final DatabaseEntity entity) {
        final HipChatGlobalConfigEntity hipChatEntity = (HipChatGlobalConfigEntity) entity;
        return repository.save(hipChatEntity);
    }

}
