export interface SkillNode {
  id: string
  name: string
  level: number
  children?: SkillNode[]
  color?: string
  link?: string
}

export interface SkillTree {
  id: string
  name: string
  root: SkillNode
}

export const JAVA_SKILL_TREE: SkillTree = {
  id: 'java',
  name: 'Java 技术栈',
  root: {
    id: 'java-root',
    name: 'Java',
    level: 0,
    color: '#2563eb',
    children: [
      {
        id: 'java-se',
        name: 'Java SE',
        level: 1,
        color: '#3b82f6',
        children: [
          {
            id: 'collections',
            name: '集合框架',
            level: 2,
            color: '#60a5fa',
            children: [
              { id: 'list', name: 'List', level: 3, color: '#93c5fd', children: [
                { id: 'arraylist', name: 'ArrayList', level: 4, color: '#bfdbfe', link: '/article/list?tag=ArrayList' },
                { id: 'linkedlist', name: 'LinkedList', level: 4, color: '#bfdbfe', link: '/article/list?tag=LinkedList' },
                { id: 'vector', name: 'Vector', level: 4, color: '#bfdbfe' },
              ]},
              { id: 'map', name: 'Map', level: 3, color: '#93c5fd', children: [
                { id: 'hashmap', name: 'HashMap', level: 4, color: '#bfdbfe', link: '/article/list?tag=HashMap' },
                { id: 'linkedhashmap', name: 'LinkedHashMap', level: 4, color: '#bfdbfe' },
                { id: 'treemap', name: 'TreeMap', level: 4, color: '#bfdbfe' },
                { id: 'concurrentmap', name: 'ConcurrentHashMap', level: 4, color: '#bfdbfe', link: '/article/list?tag=ConcurrentHashMap' },
              ]},
              { id: 'set', name: 'Set', level: 3, color: '#93c5fd', children: [
                { id: 'hashset', name: 'HashSet', level: 4, color: '#bfdbfe' },
                { id: 'treeset', name: 'TreeSet', level: 4, color: '#bfdbfe' },
              ]},
              { id: 'queue', name: 'Queue', level: 3, color: '#93c5fd', children: [
                { id: 'priorityqueue', name: 'PriorityQueue', level: 4, color: '#bfdbfe' },
                { id: 'blockingqueue', name: 'BlockingQueue', level: 4, color: '#bfdbfe' },
              ]},
            ]
          },
          {
            id: 'stream',
            name: 'Stream API',
            level: 2,
            color: '#60a5fa',
            children: [
              { id: 'stream-create', name: '创建流', level: 3, color: '#93c5fd' },
              { id: 'stream-op', name: '中间操作', level: 3, color: '#93c5fd', children: [
                { id: 'filter', name: 'filter', level: 4, color: '#bfdbfe' },
                { id: 'map', name: 'map', level: 4, color: '#bfdbfe' },
                { id: 'sorted', name: 'sorted', level: 4, color: '#bfdbfe' },
              ]},
              { id: 'stream-terminal', name: '终止操作', level: 3, color: '#93c5fd', children: [
                { id: 'collect', name: 'collect', level: 4, color: '#bfdbfe' },
                { id: 'foreach', name: 'forEach', level: 4, color: '#bfdbfe' },
                { id: 'reduce', name: 'reduce', level: 4, color: '#bfdbfe' },
              ]},
              { id: 'parallel-stream', name: '并行流', level: 3, color: '#93c5fd' },
            ]
          },
          {
            id: 'lambda',
            name: 'Lambda 表达式',
            level: 2,
            color: '#60a5fa',
            children: [
              { id: 'lambda-syntax', name: '语法糖', level: 3, color: '#93c5fd' },
              { id: 'method-ref', name: '方法引用', level: 3, color: '#93c5fd' },
              { id: 'func-interface', name: '函数式接口', level: 3, color: '#93c5fd', children: [
                { id: 'predicate', name: 'Predicate', level: 4, color: '#bfdbfe' },
                { id: 'function', name: 'Function', level: 4, color: '#bfdbfe' },
                { id: 'consumer', name: 'Consumer', level: 4, color: '#bfdbfe' },
              ]},
            ]
          },
          {
            id: 'reflection',
            name: '反射与注解',
            level: 2,
            color: '#60a5fa',
            children: [
              { id: 'reflect-api', name: '反射 API', level: 3, color: '#93c5fd' },
              { id: 'annotation', name: '注解', level: 3, color: '#93c5fd', children: [
                { id: 'built-in-anno', name: '内置注解', level: 4, color: '#bfdbfe' },
                { id: 'meta-anno', name: '元注解', level: 4, color: '#bfdbfe' },
                { id: 'custom-anno', name: '自定义注解', level: 4, color: '#bfdbfe' },
              ]},
            ]
          },
          {
            id: 'io',
            name: 'IO/NIO',
            level: 2,
            color: '#60a5fa',
            children: [
              { id: 'bio', name: 'BIO', level: 3, color: '#93c5fd' },
              { id: 'nio', name: 'NIO', level: 3, color: '#93c5fd', children: [
                { id: 'buffer', name: 'Buffer', level: 4, color: '#bfdbfe' },
                { id: 'channel', name: 'Channel', level: 4, color: '#bfdbfe' },
                { id: 'selector', name: 'Selector', level: 4, color: '#bfdbfe' },
              ]},
              { id: 'aio', name: 'AIO', level: 3, color: '#93c5fd' },
            ]
          },
        ]
      },
      {
        id: 'concurrency',
        name: '并发编程',
        level: 1,
        color: '#10b981',
        children: [
          {
            id: 'juc',
            name: 'JUC 并发包',
            level: 2,
            color: '#34d399',
            children: [
              { id: 'atomic', name: '原子类', level: 3, color: '#6ee7b7', children: [
                { id: 'atomicinteger', name: 'AtomicInteger', level: 4, color: '#a7f3d0' },
                { id: 'atomiclong', name: 'AtomicLong', level: 4, color: '#a7f3d0' },
                { id: 'atomicref', name: 'AtomicReference', level: 4, color: '#a7f3d0' },
              ]},
              { id: 'locks', name: '锁', level: 3, color: '#6ee7b7', children: [
                { id: 'reentrantlock', name: 'ReentrantLock', level: 4, color: '#a7f3d0', link: '/article/list?tag=ReentrantLock' },
                { id: 'readwritelock', name: 'ReadWriteLock', level: 4, color: '#a7f3d0' },
                { id: 'stampedlock', name: 'StampedLock', level: 4, color: '#a7f3d0' },
              ]},
              { id: 'collections-concurrent', name: '并发集合', level: 3, color: '#6ee7b7', children: [
                { id: 'concurrenthashmap', name: 'ConcurrentHashMap', level: 4, color: '#a7f3d0' },
                { id: 'blockingqueue', name: 'BlockingQueue', level: 4, color: '#a7f3d0' },
              ]},
              { id: 'executors', name: '并发工具', level: 3, color: '#6ee7b7', children: [
                { id: 'countdownlatch', name: 'CountDownLatch', level: 4, color: '#a7f3d0' },
                { id: 'cyclicbarrier', name: 'CyclicBarrier', level: 4, color: '#a7f3d0' },
                { id: 'semaphore', name: 'Semaphore', level: 4, color: '#a7f3d0' },
              ]},
            ]
          },
          {
            id: 'threadpool',
            name: '线程池',
            level: 2,
            color: '#34d399',
            children: [
              { id: 'threadpoolcreator', name: '线程池创建', level: 3, color: '#6ee7b7' },
              { id: 'threadpoolparams', name: '参数调优', level: 3, color: '#6ee7b7' },
              { id: 'threadpool-reject', name: '拒绝策略', level: 3, color: '#6ee7b7' },
              { id: 'future', name: 'Future', level: 3, color: '#6ee7b7' },
            ]
          },
          {
            id: 'lock-mechanism',
            name: '锁机制',
            level: 2,
            color: '#34d399',
            children: [
              { id: 'synchronized', name: 'synchronized', level: 3, color: '#6ee7b7', link: '/article/list?tag=synchronized' },
              { id: 'volatile', name: 'volatile', level: 3, color: '#6ee7b7', link: '/article/list?tag=volatile' },
              { id: 'aqs', name: 'AQS 原理', level: 3, color: '#6ee7b7', link: '/article/list?tag=AQS' },
              { id: 'cas', name: 'CAS 原理', level: 3, color: '#6ee7b7' },
            ]
          },
          {
            id: 'jmm',
            name: '内存模型 JMM',
            level: 2,
            color: '#34d399',
            children: [
              { id: 'jmm-rule', name: 'JMM 规则', level: 3, color: '#6ee7b7' },
              { id: 'happens-before', name: 'Happens-Before', level: 3, color: '#6ee7b7' },
              { id: 'memory-barrier', name: '内存屏障', level: 3, color: '#6ee7b7' },
              { id: 'final', name: 'final 内存语义', level: 3, color: '#6ee7b7' },
            ]
          },
          {
            id: 'thread-comm',
            name: '线程通信',
            level: 2,
            color: '#34d399',
            children: [
              { id: 'wait-notify', name: 'wait/notify', level: 3, color: '#6ee7b7' },
              { id: 'park-unpark', name: 'Park/Unpark', level: 3, color: '#6ee7b7' },
              { id: 'join', name: 'join', level: 3, color: '#6ee7b7' },
            ]
          },
        ]
      },
      {
        id: 'jvm',
        name: 'JVM',
        level: 1,
        color: '#8b5cf6',
        children: [
          {
            id: 'classloader',
            name: '类加载机制',
            level: 2,
            color: '#a78bfa',
            children: [
              { id: 'classloader-types', name: '类加载器类型', level: 3, color: '#c4b5fd', children: [
                { id: 'bootstrap', name: 'Bootstrap', level: 4, color: '#ddd6fe' },
                { id: 'extension', name: 'Extension', level: 4, color: '#ddd6fe' },
                { id: 'application', name: 'Application', level: 4, color: '#ddd6fe' },
                { id: 'custom', name: '自定义加载器', level: 4, color: '#ddd6fe' },
              ]},
              { id: 'loading-step', name: '加载步骤', level: 3, color: '#c4b5fd' },
              { id: 'parent-delegation', name: '双亲委派', level: 3, color: '#c4b5fd', link: '/article/list?tag=双亲委派' },
              { id: 'spi', name: 'SPI', level: 3, color: '#c4b5fd' },
            ]
          },
          {
            id: 'gc',
            name: '垃圾回收 GC',
            level: 2,
            color: '#a78bfa',
            children: [
              { id: 'gc-algorithm', name: 'GC 算法', level: 3, color: '#c4b5fd', children: [
                { id: 'mark-sweep', name: '标记-清除', level: 4, color: '#ddd6fe' },
                { id: 'copying', name: '复制', level: 4, color: '#ddd6fe' },
                { id: 'mark-compact', name: '标记-整理', level: 4, color: '#ddd6fe' },
                { id: 'gc-generational', name: '分代收集', level: 4, color: '#ddd6fe' },
              ]},
              { id: 'gc-collector', name: '垃圾收集器', level: 3, color: '#c4b5fd', children: [
                { id: 'serial', name: 'Serial', level: 4, color: '#ddd6fe' },
                { id: 'parallel', name: 'Parallel', level: 4, color: '#ddd6fe' },
                { id: 'cms', name: 'CMS', level: 4, color: '#ddd6fe' },
                { id: 'g1', name: 'G1', level: 4, color: '#ddd6fe', link: '/article/list?tag=G1' },
                { id: 'zgc', name: 'ZGC', level: 4, color: '#ddd6fe' },
              ]},
              { id: 'memory-model', name: '内存布局', level: 3, color: '#c4b5fd', children: [
                { id: 'heap', name: '堆内存', level: 4, color: '#ddd6fe' },
                { id: 'young-gen', name: '年轻代', level: 4, color: '#ddd6fe' },
                { id: 'old-gen', name: '老年代', level: 4, color: '#ddd6fe' },
                { id: 'metaspace', name: 'Metaspace', level: 4, color: '#ddd6fe' },
              ]},
            ]
          },
          {
            id: 'bytecode',
            name: '字节码执行',
            level: 2,
            color: '#a78bfa',
            children: [
              { id: 'bytecode-instruction', name: '字节码指令', level: 3, color: '#c4b5fd' },
              { id: 'invokedynamic', name: 'InvokeDynamic', level: 3, color: '#c4b5fd' },
              { id: 'asm', name: 'ASM/Javassist', level: 3, color: '#c4b5fd' },
            ]
          },
          {
            id: 'jvm-tuning',
            name: '性能调优',
            level: 2,
            color: '#a78bfa',
            children: [
              { id: 'jvm-params', name: 'JVM 参数', level: 3, color: '#c4b5fd' },
              { id: 'oom', name: 'OOM 排查', level: 3, color: '#c4b5fd' },
              { id: 'arthas', name: 'Arthas', level: 3, color: '#c4b5fd' },
            ]
          },
        ]
      },
      {
        id: 'framework',
        name: '框架生态',
        level: 1,
        color: '#f59e0b',
        children: [
          {
            id: 'spring',
            name: 'Spring 家族',
            level: 2,
            color: '#fbbf24',
            children: [
              { id: 'spring-core', name: 'Spring Core', level: 3, color: '#fcd34d', children: [
                { id: 'ioc', name: 'IoC 容器', level: 4, color: '#fef3c7', link: '/article/list?tag=IoC' },
                { id: 'di', name: '依赖注入', level: 4, color: '#fef3c7' },
                { id: 'bean', name: 'Bean 生命周期', level: 4, color: '#fef3c7' },
              ]},
              { id: 'spring-aop', name: 'AOP', level: 3, color: '#fcd34d', children: [
                { id: 'aop-concept', name: 'AOP 概念', level: 4, color: '#fef3c7' },
                { id: 'aspectj', name: 'AspectJ', level: 4, color: '#fef3c7' },
                { id: 'aop-proxy', name: '代理机制', level: 4, color: '#fef3c7' },
              ]},
              { id: 'springboot', name: 'Spring Boot', level: 3, color: '#fcd34d', children: [
                { id: 'autoconfig', name: '自动配置', level: 4, color: '#fef3c7', link: '/article/list?tag=自动配置' },
                { id: 'starter', name: 'Starter 生态', level: 4, color: '#fef3c7' },
                { id: 'actuator', name: 'Actuator', level: 4, color: '#fef3c7' },
              ]},
              { id: 'springcloud', name: 'Spring Cloud', level: 3, color: '#fcd34d', children: [
                { id: 'nacos', name: 'Nacos', level: 4, color: '#fef3c7' },
                { id: 'sentinel', name: 'Sentinel', level: 4, color: '#fef3c7' },
                { id: 'feign', name: 'Feign', level: 4, color: '#fef3c7' },
                { id: 'gateway', name: 'Gateway', level: 4, color: '#fef3c7' },
              ]},
              { id: 'springtransaction', name: '事务管理', level: 3, color: '#fcd34d', children: [
                { id: 'tx-annotation', name: '@Transactional', level: 4, color: '#fef3c7' },
                { id: 'tx-propagation', name: '传播行为', level: 4, color: '#fef3c7' },
                { id: 'tx-isolation', name: '隔离级别', level: 4, color: '#fef3c7' },
              ]},
              { id: 'springmvc', name: 'Spring MVC', level: 3, color: '#fcd34d', children: [
                { id: 'dispatcher', name: 'DispatcherServlet', level: 4, color: '#fef3c7' },
                { id: 'handler-mapping', name: 'HandlerMapping', level: 4, color: '#fef3c7' },
                { id: 'interceptor', name: 'Interceptor', level: 4, color: '#fef3c7' },
              ]},
            ]
          },
          {
            id: 'mybatis',
            name: 'MyBatis 生态',
            level: 2,
            color: '#fbbf24',
            children: [
              { id: 'mybatis-config', name: '配置与映射', level: 3, color: '#fcd34d' },
              { id: 'mybatis-dynamic', name: '动态 SQL', level: 3, color: '#fcd34d' },
              { id: 'mybatis-cache', name: '缓存机制', level: 3, color: '#fcd34d' },
              { id: 'mybatis-plus', name: 'MyBatis-Plus', level: 3, color: '#fcd34d', link: '/article/list?tag=MyBatis-Plus' },
              { id: 'mybatis-plugin', name: '插件机制', level: 3, color: '#fcd34d' },
            ]
          },
          {
            id: 'orm',
            name: 'ORM 框架',
            level: 2,
            color: '#fbbf24',
            children: [
              { id: 'hibernate', name: 'Hibernate', level: 3, color: '#fcd34d' },
              { id: 'jpa', name: 'JPA', level: 3, color: '#fcd34d' },
              { id: 'springdata', name: 'Spring Data', level: 3, color: '#fcd34d' },
            ]
          },
        ]
      },
      {
        id: 'distributed',
        name: '分布式系统',
        level: 1,
        color: '#ef4444',
        children: [
          { id: 'microservice', name: '微服务架构', level: 2, color: '#f87171', children: [
            { id: 'service-mesh', name: '服务网格', level: 3, color: '#fca5a5' },
            { id: 'istio', name: 'Istio', level: 3, color: '#fca5a5' },
            { id: 'dubbo', name: 'Dubbo', level: 3, color: '#fca5a5' },
          ]},
          { id: 'mq', name: '消息队列', level: 2, color: '#f87171', children: [
            { id: 'kafka', name: 'Kafka', level: 3, color: '#fca5a5', link: '/article/list?tag=Kafka' },
            { id: 'rocketmq', name: 'RocketMQ', level: 3, color: '#fca5a5' },
            { id: 'rabbitmq', name: 'RabbitMQ', level: 3, color: '#fca5a5' },
          ]},
          { id: 'cache', name: '缓存系统', level: 2, color: '#f87171', children: [
            { id: 'redis', name: 'Redis', level: 3, color: '#fca5a5', link: '/article/list?tag=Redis' },
            { id: 'caffeine', name: 'Caffeine', level: 3, color: '#fca5a5' },
            { id: 'cache-strategy', name: '缓存策略', level: 3, color: '#fca5a5' },
            { id: 'cache-pen', name: '缓存穿透/击穿/雪崩', level: 3, color: '#fca5a5' },
          ]},
          { id: 'registry', name: '注册中心', level: 2, color: '#f87171', children: [
            { id: 'eureka', name: 'Eureka', level: 3, color: '#fca5a5' },
            { id: 'nacos', name: 'Nacos', level: 3, color: '#fca5a5' },
            { id: 'consul', name: 'Consul', level: 3, color: '#fca5a5' },
            { id: 'zookeeper', name: 'Zookeeper', level: 3, color: '#fca5a5' },
          ]},
          { id: 'distributed-lock', name: '分布式锁', level: 2, color: '#f87171', children: [
            { id: 'redis-lock', name: 'Redis 分布式锁', level: 3, color: '#fca5a5' },
            { id: 'zk-lock', name: 'ZooKeeper 锁', level: 3, color: '#fca5a5' },
            { id: 'redlock', name: 'RedLock', level: 3, color: '#fca5a5' },
          ]},
          { id: 'distributed-transaction', name: '分布式事务', level: 2, color: '#f87171', children: [
            { id: 'seata', name: 'Seata', level: 3, color: '#fca5a5' },
            { id: 'saga', name: 'Saga 模式', level: 3, color: '#fca5a5' },
            { id: 'tcc', name: 'TCC 模式', level: 3, color: '#fca5a5' },
          ]},
        ]
      },
      {
        id: 'database',
        name: '数据库',
        level: 1,
        color: '#06b6d4',
        children: [
          { id: 'mysql', name: 'MySQL', level: 2, color: '#22d3ee', children: [
            { id: 'mysql-arch', name: '架构与存储引擎', level: 3, color: '#67e8f9', children: [
              { id: 'innodb', name: 'InnoDB', level: 4, color: '#a5f3fc', link: '/article/list?tag=InnoDB' },
              { id: 'myisam', name: 'MyISAM', level: 4, color: '#a5f3fc' },
            ]},
            { id: 'mysql-index', name: '索引', level: 3, color: '#67e8f9', children: [
              { id: 'b-tree', name: 'B+Tree', level: 4, color: '#a5f3fc' },
              { id: 'hash-index', name: 'Hash 索引', level: 4, color: '#a5f3fc' },
              { id: 'index-optimization', name: '索引优化', level: 4, color: '#a5f3fc' },
            ]},
            { id: 'mysql-transaction', name: '事务与锁', level: 3, color: '#67e8f9', children: [
              { id: 'acid', name: 'ACID', level: 4, color: '#a5f3fc' },
              { id: 'mvcc', name: 'MVCC', level: 4, color: '#a5f3fc', link: '/article/list?tag=MVCC' },
              { id: 'lock-type', name: '锁类型', level: 4, color: '#a5f3fc' },
            ]},
            { id: 'mysql-optimization', name: '性能优化', level: 3, color: '#67e8f9', children: [
              { id: 'explain', name: 'EXPLAIN', level: 4, color: '#a5f3fc' },
              { id: 'slow-query', name: '慢查询优化', level: 4, color: '#a5f3fc' },
              { id: 'sql-optimization', name: 'SQL 优化', level: 4, color: '#a5f3fc' },
            ]},
          ]},
          { id: 'nosql', name: 'NoSQL', level: 2, color: '#22d3ee', children: [
            { id: 'mongodb', name: 'MongoDB', level: 3, color: '#67e8f9' },
            { id: 'elasticsearch', name: 'Elasticsearch', level: 3, color: '#67e8f9', link: '/article/list?tag=Elasticsearch' },
            { id: 'hbase', name: 'HBase', level: 3, color: '#67e8f9' },
          ]},
        ]
      },
      {
        id: 'devops',
        name: 'DevOps',
        level: 1,
        color: '#84cc16',
        children: [
          { id: 'docker', name: 'Docker', level: 2, color: '#a3e635', link: '/article/list?tag=Docker' },
          { id: 'kubernetes', name: 'Kubernetes', level: 2, color: '#a3e635', link: '/article/list?tag=Kubernetes' },
          { id: 'ci-cd', name: 'CI/CD', level: 2, color: '#a3e635' },
          { id: 'jenkins', name: 'Jenkins', level: 2, color: '#a3e635' },
          { id: 'git', name: 'Git', level: 2, color: '#a3e635' },
        ]
      },
      {
        id: 'tools',
        name: '工具类',
        level: 1,
        color: '#ec4899',
        children: [
          { id: 'lombok', name: 'Lombok', level: 2, color: '#f472b6' },
          { id: 'hutool', name: 'Hutool', level: 2, color: '#f472b6' },
          { id: 'guava', name: 'Guava', level: 2, color: '#f472b6' },
          { id: 'apache-commons', name: 'Apache Commons', level: 2, color: '#f472b6' },
          { id: 'fastjson', name: 'FastJSON', level: 2, color: '#f472b6' },
          { id: 'jackson', name: 'Jackson', level: 2, color: '#f472b6' },
        ]
      },
    ]
  }
}

export const SKILL_TREES: SkillTree[] = [JAVA_SKILL_TREE]
