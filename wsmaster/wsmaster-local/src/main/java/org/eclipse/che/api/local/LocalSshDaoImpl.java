/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.local;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.reflect.TypeToken;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.local.storage.LocalStorage;
import org.eclipse.che.api.local.storage.LocalStorageFactory;
import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.lang.String.format;

/**
 * In-memory implementation of {@link SshDao}
 *
 * @author Sergii Leschenko
 */
@Singleton
public class LocalSshDaoImpl implements SshDao {
    private final ListMultimap<String, SshPairImpl> pairs;
    private final ReadWriteLock                     lock;
    private final LocalStorage                      sshStorage;

    @Inject
    public LocalSshDaoImpl(LocalStorageFactory storageFactory) throws IOException {
        pairs = ArrayListMultimap.create();
        lock = new ReentrantReadWriteLock();
        sshStorage = storageFactory.create("ssh.json");
    }

    @Override
    public void create(String owner, SshPairImpl usersSshPair) throws ConflictException {
        lock.writeLock().lock();
        try {
            final Optional<SshPairImpl> any = find(owner, usersSshPair.getService(), usersSshPair.getName());
            if (any.isPresent()) {
                throw new ConflictException(format("Ssh pair with service '%s' and name %s already exist.",
                                                   usersSshPair.getService(),
                                                   usersSshPair.getName()));
            }
            pairs.put(owner, usersSshPair);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public SshPairImpl get(String owner, String service, String name) throws NotFoundException {
        lock.readLock().lock();
        try {
            final Optional<SshPairImpl> any = find(owner, service, name);
            if (any.isPresent()) {
                return any.get();
            }
            throw new NotFoundException(format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void remove(String owner, String service, String name) throws NotFoundException {
        lock.writeLock().lock();
        try {
            final Optional<SshPairImpl> any = find(owner, service, name);
            if (!any.isPresent()) {
                throw new NotFoundException(format("Ssh pair with service '%s' and name '%s' was not found.", service, name));
            }
            pairs.remove(owner, any.get());
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<SshPairImpl> get(String owner, String service) {
        lock.readLock().lock();
        try {
            return pairs.get(owner)
                        .stream()
                        .filter(sshPair -> sshPair.getService().equals(service))
                        .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    private Optional<SshPairImpl> find(String owner, String service, String name) {
        return pairs.get(owner)
                    .stream()
                    .filter(sshPair -> sshPair.getService().equals(service)
                                       && sshPair.getName().equals(name))
                    .findAny();
    }

    @PostConstruct
    @VisibleForTesting
    void loadSshPairs() {
        lock.writeLock().lock();
        try {
            final Map<String, List<SshPairImpl>> ownerToPairs = sshStorage.loadMap(new TypeToken<Map<String, List<SshPairImpl>>>() {});
            for (Map.Entry<String, List<SshPairImpl>> stringListEntry : ownerToPairs.entrySet()) {
                for (SshPairImpl sshPair : stringListEntry.getValue()) {
                    pairs.put(stringListEntry.getKey(), sshPair);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @PreDestroy
    @VisibleForTesting
    void saveSshPairs() throws IOException {
        lock.readLock().lock();
        try {
            final HashMap<String, List<SshPairImpl>> ownerToPairs = new HashMap<>();
            for (Map.Entry<String, SshPairImpl> entry : pairs.entries()) {
                ownerToPairs.computeIfAbsent(entry.getKey(), s -> new ArrayList<>());
                ownerToPairs.get(entry.getKey()).add(entry.getValue());
            }
            sshStorage.store(ownerToPairs);
        } finally {
            lock.readLock().unlock();
        }
    }
}
