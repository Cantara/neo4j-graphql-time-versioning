package com.exoreaction.xorcery.tbv.api.persistence;

import com.exoreaction.xorcery.tbv.api.persistence.reactivex.RxJsonPersistence;
import com.exoreaction.xorcery.tbv.api.specification.Specification;

import java.util.Map;
import java.util.Set;

public interface PersistenceInitializer {

    String persistenceProviderId();

    Set<String> configurationKeys();

    RxJsonPersistence initialize(String defaultNamespace, Map<String, String> configuration, Set<String> managedDomains, Specification specification);
}
