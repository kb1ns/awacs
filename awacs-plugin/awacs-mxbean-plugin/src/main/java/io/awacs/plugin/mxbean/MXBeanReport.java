/**
 * Copyright 2016 AWACS Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.awacs.plugin.mxbean;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by antong on 16/9/28.
 */
public class MXBeanReport {
    private ClassLoading classLoading;
    private Memory memory;
    private Thread thread;
    private Runtime runtime;
    private OperatingSystem operatingSystem;
    private List<MemoryManager> memoryManagers;
    private List<GarbageCollector> garbageCollectors;
    private List<MemoryPool> memoryPools;

    public MXBeanReport() {
        this.classLoading = ClassLoading.build();
        this.memory = Memory.build();
        this.thread = Thread.build();
        this.runtime = Runtime.build();
        this.operatingSystem = OperatingSystem.build();
        this.memoryManagers = MemoryManager.build();
        this.garbageCollectors = GarbageCollector.build();
        this.memoryPools = MemoryPool.build();
    }

    public static class ClassLoading {
        private int loadedClassCount;
        private long totalLoadedClassCount;
        private long unloadedClassCount;

        public ClassLoading(ClassLoadingMXBean bean) {
            this.loadedClassCount = bean.getLoadedClassCount();
            this.totalLoadedClassCount = bean.getTotalLoadedClassCount();
            this.unloadedClassCount = bean.getUnloadedClassCount();
        }

        public static ClassLoading build() {
            ClassLoadingMXBean bean = ManagementFactory.getClassLoadingMXBean();
            return new ClassLoading(bean);
        }

        public int getLoadedClassCount() {
            return loadedClassCount;
        }

        public void setLoadedClassCount(int loadedClassCount) {
            this.loadedClassCount = loadedClassCount;
        }

        public long getTotalLoadedClassCount() {
            return totalLoadedClassCount;
        }

        public void setTotalLoadedClassCount(long totalLoadedClassCount) {
            this.totalLoadedClassCount = totalLoadedClassCount;
        }

        public long getUnloadedClassCount() {
            return unloadedClassCount;
        }

        public void setUnloadedClassCount(long unloadedClassCount) {
            this.unloadedClassCount = unloadedClassCount;
        }
    }

    public static class Memory {
        private String heapMemoryUsage;
        private String NonHeapMemoryUsage;

        public Memory(MemoryMXBean bean) {
            this.heapMemoryUsage = bean.getHeapMemoryUsage().toString();
            this.NonHeapMemoryUsage = bean.getNonHeapMemoryUsage().toString();
        }

        public static Memory build() {
            MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
            return new Memory(bean);
        }

        public String getHeapMemoryUsage() {
            return heapMemoryUsage;
        }

        public void setHeapMemoryUsage(String heapMemoryUsage) {
            this.heapMemoryUsage = heapMemoryUsage;
        }

        public String getNonHeapMemoryUsage() {
            return NonHeapMemoryUsage;
        }

        public void setNonHeapMemoryUsage(String nonHeapMemoryUsage) {
            NonHeapMemoryUsage = nonHeapMemoryUsage;
        }
    }

    public static class Thread {
        private long[] threadIds;
        private long currentThreadCpuTime;
        private long currentThreadUserTime;
        private int damonThreadCount;
        private int peakThreadCount;
        private int threadCount;

        public Thread(ThreadMXBean bean) {
            this.threadIds = bean.getAllThreadIds();
            this.currentThreadCpuTime = bean.getCurrentThreadCpuTime();
            this.currentThreadUserTime = bean.getCurrentThreadUserTime();
            this.damonThreadCount = bean.getDaemonThreadCount();
            this.peakThreadCount = bean.getPeakThreadCount();
            this.threadCount = bean.getThreadCount();
        }

        public static Thread build() {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            return new Thread(bean);
        }

        public long[] getThreadIds() {
            return threadIds;
        }

        public void setThreadIds(long[] threadIds) {
            this.threadIds = threadIds;
        }

        public long getCurrentThreadCpuTime() {
            return currentThreadCpuTime;
        }

        public void setCurrentThreadCpuTime(long currentThreadCpuTime) {
            this.currentThreadCpuTime = currentThreadCpuTime;
        }

        public long getCurrentThreadUserTime() {
            return currentThreadUserTime;
        }

        public void setCurrentThreadUserTime(long currentThreadUserTime) {
            this.currentThreadUserTime = currentThreadUserTime;
        }

        public int getDamonThreadCount() {
            return damonThreadCount;
        }

        public void setDamonThreadCount(int damonThreadCount) {
            this.damonThreadCount = damonThreadCount;
        }

        public int getPeakThreadCount() {
            return peakThreadCount;
        }

        public void setPeakThreadCount(int peakThreadCount) {
            this.peakThreadCount = peakThreadCount;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public void setThreadCount(int threadCount) {
            this.threadCount = threadCount;
        }
    }

    public static class Runtime {
        private String bootClassPath;
        private String classPath;
        private List<String> inputArguments;
        private String libraryPath;
        private String managementSpecVersion;

        public Runtime(RuntimeMXBean bean) {
            this.bootClassPath = bean.getBootClassPath();
            this.classPath = bean.getClassPath();
            this.inputArguments = bean.getInputArguments();
            this.libraryPath = bean.getLibraryPath();
            this.managementSpecVersion = bean.getManagementSpecVersion();
        }

        public static Runtime build() {
            RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
            return new Runtime(bean);
        }

        public String getBootClassPath() {
            return bootClassPath;
        }

        public void setBootClassPath(String bootClassPath) {
            this.bootClassPath = bootClassPath;
        }

        public String getClassPath() {
            return classPath;
        }

        public void setClassPath(String classPath) {
            this.classPath = classPath;
        }

        public List<String> getInputArguments() {
            return inputArguments;
        }

        public void setInputArguments(List<String> inputArguments) {
            this.inputArguments = inputArguments;
        }

        public String getLibraryPath() {
            return libraryPath;
        }

        public void setLibraryPath(String libraryPath) {
            this.libraryPath = libraryPath;
        }

        public String getManagementSpecVersion() {
            return managementSpecVersion;
        }

        public void setManagementSpecVersion(String managementSpecVersion) {
            this.managementSpecVersion = managementSpecVersion;
        }
    }

    private static class OperatingSystem {
        private String arch;
        private int availableProcessors;
        private String name;
        private String version;

        public OperatingSystem(OperatingSystemMXBean bean) {
            this.arch = bean.getArch();
            this.availableProcessors = bean.getAvailableProcessors();
            this.name = bean.getName();
            this.version = bean.getVersion();
        }

        public static OperatingSystem build() {
            OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
            return new OperatingSystem(bean);
        }

        public String getArch() {
            return arch;
        }

        public void setArch(String arch) {
            this.arch = arch;
        }

        public int getAvailableProcessors() {
            return availableProcessors;
        }

        public void setAvailableProcessors(int availableProcessors) {
            this.availableProcessors = availableProcessors;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    private static class MemoryManager {
        private String[] memoryPoolNames;
        private String name;

        public MemoryManager(MemoryManagerMXBean bean) {
            this.memoryPoolNames = bean.getMemoryPoolNames();
            this.name = bean.getName();
        }

        public static List<MemoryManager> build() {
            List<MemoryManager> list = new ArrayList<>();
            List<MemoryManagerMXBean> beans = ManagementFactory.getMemoryManagerMXBeans();
            for (MemoryManagerMXBean bean : beans) {
                list.add(new MemoryManager(bean));
            }
            return list;
        }

        public String[] getMemoryPoolNames() {
            return memoryPoolNames;
        }

        public void setMemoryPoolNames(String[] memoryPoolNames) {
            this.memoryPoolNames = memoryPoolNames;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private static class MemoryPool {
        private String name;
        private String type;
        private String usage;
        private String peakUsage;
        private boolean valid;
        private String[] memoryManagerNames;
        private long usageThreshold;
        private boolean isUsageThresholdExceeded;
        private long usageThresholdCount;
        private boolean isUsageThresholdSupported;
        private long collectionUsageThreshold;
        private boolean isCollectionUsageThresholdExceeded;
        private long collectionUsageThresholdCount;
        private String collectionUsage;

        public MemoryPool(MemoryPoolMXBean bean) {
            this.name = bean.getName();
            this.type = bean.getType().toString();
            this.usage = bean.getUsage().toString();
            this.peakUsage = bean.getPeakUsage().toString();
            this.valid = bean.isValid();
            this.memoryManagerNames = bean.getMemoryManagerNames();
//            this.usageThreshold = bean.getUsageThreshold();
//            this.isUsageThresholdExceeded = bean.isUsageThresholdExceeded();
//            usageThresholdCount = bean.getUsageThresholdCount();
//            this.isUsageThresholdSupported = bean.isUsageThresholdSupported();
            //todo
//            collectionUsageThreshold = bean.getCollectionUsageThreshold();
//            this.isCollectionUsageThresholdExceeded = bean.isCollectionUsageThresholdExceeded();
//            collectionUsageThresholdCount = bean.getCollectionUsageThresholdCount();
//            collectionUsage = bean.getCollectionUsage().toString();
            collectionUsageThreshold = 0;
            this.isCollectionUsageThresholdExceeded = false;
            collectionUsageThresholdCount = 0;
            collectionUsage = "";
        }

        public static List<MemoryPool> build() {
            List<MemoryPool> list = new ArrayList<>();
            List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
            for (MemoryPoolMXBean bean : beans) {
                list.add(new MemoryPool(bean));
            }
            return list;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUsage() {
            return usage;
        }

        public void setUsage(String usage) {
            this.usage = usage;
        }

        public String getPeakUsage() {
            return peakUsage;
        }

        public void setPeakUsage(String peakUsage) {
            this.peakUsage = peakUsage;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String[] getMemoryManagerNames() {
            return memoryManagerNames;
        }

        public void setMemoryManagerNames(String[] memoryManagerNames) {
            this.memoryManagerNames = memoryManagerNames;
        }

        public long getUsageThreshold() {
            return usageThreshold;
        }

        public void setUsageThreshold(long usageThreshold) {
            this.usageThreshold = usageThreshold;
        }

        public boolean isUsageThresholdExceeded() {
            return isUsageThresholdExceeded;
        }

        public void setIsUsageThresholdExceeded(boolean isUsageThresholdExceeded) {
            this.isUsageThresholdExceeded = isUsageThresholdExceeded;
        }

        public long getUsageThresholdCount() {
            return usageThresholdCount;
        }

        public void setUsageThresholdCount(long usageThresholdCount) {
            this.usageThresholdCount = usageThresholdCount;
        }

        public boolean isUsageThresholdSupported() {
            return isUsageThresholdSupported;
        }

        public void setIsUsageThresholdSupported(boolean isUsageThresholdSupported) {
            this.isUsageThresholdSupported = isUsageThresholdSupported;
        }

        public long getCollectionUsageThreshold() {
            return collectionUsageThreshold;
        }

        public void setCollectionUsageThreshold(long collectionUsageThreshold) {
            this.collectionUsageThreshold = collectionUsageThreshold;
        }

        public boolean isCollectionUsageThresholdExceeded() {
            return isCollectionUsageThresholdExceeded;
        }

        public void setIsCollectionUsageThresholdExceeded(boolean isCollectionUsageThresholdExceeded) {
            this.isCollectionUsageThresholdExceeded = isCollectionUsageThresholdExceeded;
        }

        public long getCollectionUsageThresholdCount() {
            return collectionUsageThresholdCount;
        }

        public void setCollectionUsageThresholdCount(long collectionUsageThresholdCount) {
            this.collectionUsageThresholdCount = collectionUsageThresholdCount;
        }

        public String getCollectionUsage() {
            return collectionUsage;
        }

        public void setCollectionUsage(String collectionUsage) {
            this.collectionUsage = collectionUsage;
        }
    }

    private static class GarbageCollector {
        private String name;
        private long collectionTime;
        private long collectionCount;
        private String[] memoryPoolNames;

        public GarbageCollector(GarbageCollectorMXBean bean) {
            this.name = bean.getName();
            this.collectionTime = bean.getCollectionTime();
            this.collectionCount = bean.getCollectionCount();
            this.memoryPoolNames = bean.getMemoryPoolNames();
        }

        public static List<GarbageCollector> build() {
            List<GarbageCollector> list = new ArrayList<>();
            List<GarbageCollectorMXBean> beans = ManagementFactory.getGarbageCollectorMXBeans();
            for (GarbageCollectorMXBean bean : beans) {
                list.add(new GarbageCollector(bean));
            }
            return list;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getCollectionTime() {
            return collectionTime;
        }

        public void setCollectionTime(long collectionTime) {
            this.collectionTime = collectionTime;
        }

        public long getCollectionCount() {
            return collectionCount;
        }

        public void setCollectionCount(long collectionCount) {
            this.collectionCount = collectionCount;
        }

        public String[] getMemoryPoolNames() {
            return memoryPoolNames;
        }

        public void setMemoryPoolNames(String[] memoryPoolNames) {
            this.memoryPoolNames = memoryPoolNames;
        }
    }

    public ClassLoading getClassLoading() {
        return classLoading;
    }

    public void setClassLoading(ClassLoading classLoading) {
        this.classLoading = classLoading;
    }

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public Thread getThread() {
        return thread;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }

    public OperatingSystem getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(OperatingSystem operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public List<MemoryManager> getMemoryManagers() {
        return memoryManagers;
    }

    public void setMemoryManagers(List<MemoryManager> memoryManagers) {
        this.memoryManagers = memoryManagers;
    }

    public List<GarbageCollector> getGarbageCollectors() {
        return garbageCollectors;
    }

    public void setGarbageCollectors(List<GarbageCollector> garbageCollectors) {
        this.garbageCollectors = garbageCollectors;
    }

    public List<MemoryPool> getMemoryPools() {
        return memoryPools;
    }

    public void setMemoryPools(List<MemoryPool> memoryPools) {
        this.memoryPools = memoryPools;
    }

//    public static void main(String[] args) {
//        System.out.println(JSON.toJSONString(new MXBeanReport()));
//    }

//    @Override
//    public String toString() {
//        return JSON.toJSONString(this);
//    }
}
