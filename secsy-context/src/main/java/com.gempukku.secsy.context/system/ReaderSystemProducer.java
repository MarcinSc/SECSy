package com.gempukku.secsy.context.system;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ReaderSystemProducer implements SystemProducer {
    private BufferedReader reader;

    public ReaderSystemProducer(BufferedReader reader) {
        this.reader = reader;
    }

    @Override
    public Iterable<Object> createSystems() {
        Set<Object> result = new HashSet<Object>();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                result.add(Class.forName(line).newInstance());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
