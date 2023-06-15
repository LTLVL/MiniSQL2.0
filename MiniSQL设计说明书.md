# **MiniSQL设计说明书**

##### 浙江大学2022-2023学年春夏学期《数据库系统》夏学期大程报告

## 1 总体框架

#### 1.1 实现功能分析

##### 1.1.1 总目标：

- 设计并实现一个精简型单用户_SQL_引擎 **_(DBMS)_** **_MiniSQL_**，允许用户通过字符界面输入_SQL_语句实现表的建立、删除；索引的建立、删除以及表记录的插入、删除、查找、更新。
- 通过对MiniSQL的设计与实现，提高自己的系统编程能力，加深对数据库管理系统底层设计的理解。

##### 1.1.2 需求概述

- **数据类型：** 只要求支持三种基本数据类型：`int`，`char(n)`，`float`；
- **表定义：** 一个表最多可以定义![](https://www.yuque.com/api/services/graph/generate_redirect/latex?32#card=math&code=32&id=Mgcho)个属性，各属性可以指定是否为`unique`；要求定义表时必须指定主键且主键必须为单属性；
- **索引的建立和删除：** 对于表的主键属性自动建立`B+`树索引，对于声明为`unique`的属性可以通过SQL语句由用户指定建立/删除`B+`树索引（不支持联合索引）；
- **查找记录：** 可以通过`and或者or`连接的多个条件进行查询，支持等值查询和区间查询，支持投影查询操作；
- **插入和删除记录：** 支持每次一条记录的插入操作；支持每次一条或多条记录的删除操作；支持全表数据的删除操作；支持表的删除操作；
- **工程实现**：使用源代码管理工具（如Git）进行代码管理，代码提交历史和每次提交的信息清晰明确；同时编写的代码应符合代码规范，具有良好的代码风格。

#### 1.2 系统体系结构
![](https://cdn.nlark.com/yuque/0/2022/png/25540491/1648365471553-1ceac0a4-e909-42c8-8bb9-516409e03492.png#averageHue=%23ededed&from=url&id=FFqbt&originHeight=1080&originWidth=1432&originalType=binary&ratio=2.25&rotation=0&showTitle=false&status=done&style=none&title=)
#### 1.3 设计语言与运行环境

**工具：JAVA JDK 17.0.2**

**集成开发环境：Win11 IntelliJ IDEA**

## 2 各模块实现功能

#### 2.1 _Parser_

`_Parser_`模块直接与用户交互，主要实现以下功能：

-  程序流程控制，即启动并初始化→【接收命令、处理命令、显示命令结果】循环→通过exit命令退出流程。 
-  接收并解释用户输入的命令，检查命令的语法正确性和语义正确性，对正确的命令调用Executor模块对应的处理方法，并显示执行结果，对不正确的命令显示错误信息。 

#### 2.2 _Execotor_

`_Execotor_`模块是整个系统的核心，其主要功能为提供执行SQL语句的方法，供`Parser`层调用。该类内部通过一个所有数据库的Hashmap管理数据库，根据`Parser`提供的信息确定执行规则，并调用`Record Manager`、`Index Manager`和`Catalog Manager`提供的相应接口进行执行，最后输出执行结果。

#### 2.3 _Catalog Manager_

`Catalog Manager`负责管理数据库的所有模式信息，包括：

-  数据库中所有表的定义信息，包括表的名称、表中字段（列）数、主键、定义在该表上的索引。 
-  表中每个字段的定义信息，包括字段类型、是否唯一等。 
-  数据库中所有索引的定义，包括所属表、索引建立在那个字段上等。 

`Catalog Manager`还提供访问及操作上述信息的接口，供`Executor`模块使用。

#### 2.4 _Record Manager_

`Record Manager`负责管理记录表中数据的数据文件。所有的记录以堆表（Table Heap）的形式进行组织。Record Manager 的主要功能包括：记录的插入、删除与查找操作，并对外提供相应的接口。其中记录的查找操作要求能够支持不带条件的查找、带一个条件的查找（包括等值查找、不等值查找和区间查找）、带多个条件并用and、or连接的查找。

数据文件由一个或多个Record组成，一个Record包含一张表的所有行数据块大小应与缓冲区块大小相同。一个块中包含一条至多条记录，为简单起见，只要求支持定长记录的存储，且不要求支持记录的跨块存储。

#### 2.5 _Index Manager_

Index Manager 负责数据表索引的实现和管理，包括：索引（B+树等形式）的创建和删除，索引键的等值查找，索引键的范围查找（返回对应的迭代器），以及插入和删除键值等操作，并对外提供相应的接口。

`B+`树中节点大小应与缓冲区的块大小相同，`B+`树的叉数由节点大小与索引键大小计算得到。

#### 2.6 _Buffer Manager_

`Buffer Manager`负责缓冲区的管理，主要功能有：

   1. 根据需要，从磁盘中读取指定的数据页到缓冲区中或将缓冲区中的数据页转储（Flush）到磁盘；
   2. 实现缓冲区的替换算法，当缓冲区满时选择合适的数据页进行替换；
   3. 记录缓冲区中各页的状态，如是否是脏页（Dirty Page）、是否被锁定（Pin）等；
   4. 提供缓冲区页的锁定功能，被锁定的页将不允许替换。

为提高磁盘I/O操作的效率，缓冲区与文件系统交互的单位是数据页（Page），数据页的大小应为文件系统与磁盘交互单位的整数倍。在本实验中，数据页的大小默认为 4KB。

#### 2.7 _Page_

Page为数据页类型，包含了TablePageHeader（记录表的定义信息）、FreeSpace（空闲空间信息）、IndexManager（数据库表对应的索引）、数据库表数据信息等。

## 3 内部数据形式及各模块提供的接口

#### 3.1 内部数据存放形式

##### 3.1.1 异常处理

在底层模块中，遇到异常时，会抛出Java系统自带的异常类，如`IllegalArgumentException, NullPointerException`等，但这不便于统一输出错误信息，因此，我自定义了`MyExceptionHandler`异常类，`CatalogManager`模块在`catch`到底层模块的异常后，统一抛出`MyException`，进行错误信息的输出。

`MyExceptionHandler`类定义如下：

```java
public class MyExceptionHandler extends Exception{
    public int code;
    public static final String Syntax = "Syntax error";
    public static final String Runtime = "Runtime error";
    public String message;
    @Override
    public String getMessage() {
        return message;
    }
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}
```

`status`表示自定义的错误状态码，`message`则为具体的错误信息。
###### 
##### 3.1.2 _CatalogManager_模块

`CatalogManager`对应一个DBMS实例，其中有`BufferManager`的列表，方便管理所有数据库，还有buffermanager实例，用来管理当前用户使用的数据库，还有一个`IndexInfo`的集合，用来持久化所有自动生成的主键索引及用户自定义的索引信息。
`CatologManager`还调用数据库底层方法，通过`Parser`及`Excetutor`接受用户输入的Sql语句并操作对应数据库。

##### 3.1.3 _BufferManager_数据结构

在实现了基本的位图页后，通过一个位图页加上一段连续的数据页（数据页的数量取决于位图页最大能够支持的比特数）来对磁盘文件（DB File）中数据页进行分配和回收。把一个位图页加一段连续的数据页看成数据库文件中的一个分区（Extent），再通过一个额外的元信息页来记录这些分区的信息。通过这种“套娃”的方式，来使磁盘文件能够维护更多的数据页信息。其主要逻辑如下图所示：

![](https://cdn.nlark.com/yuque/0/2022/png/25540491/1648370611392-3116a928-60ef-4df3-b0fa-5903a431729f.png#averageHue=%23e1e1e1&from=url&id=HeHaM&originHeight=146&originWidth=2248&originalType=binary&ratio=2.25&rotation=0&showTitle=false&status=done&style=none&title=)

#### 3.2 Parser 实现

**_Parser_总体流程： ** `**_Parser_**`模块直接与用户交互，负责接收并解释用户输入的命令，返回结果信息，实现程序总体流程控制，其总体设计流程如下:

1.  **语句读入**：采用`Scanner`类作为输入，`StringBuilder`类作为命令语句存储空间。首先循环从输入流中读入一行字符，添加到存储空间中，直到该行字符串存在`;`字符，表明结束，此时进行字符串截取，保留`;`之前的字符串作为当前处理命令，`;`之后的字符串作为下一条命令的开始。每次进行读取时，需要先判断上一次剩余的字符串是否存在`;`，决定是否再次进行截取。 
2.  **正则替换**: 得到当前处理命令语句后，进行正则替换，将头尾的空白字符去除，将语句内的多个空白字符替换成一个空格，得到正则替换后的命令语句。 
3.  **初步解析**：将正则替换后的命令语句进行分割，调用`String`类的`split`函数对空格进行分割，得到一个`split`数组。对`split`数组中的关键字进行初步判断，如`create`、`delete`等，若关键字对应，则跳转至该关键字的处理流程中，进行二级解析。若没有对应的关键字，则抛出异常。 
4.  **二级解析**：初步解析后，根据关键字跳转至相应的解析函数中，内部进行最终解析，根据相应的命令语句调用`API`模块函数完成交互，显示结果，或根据语法错误抛出异常。共有`create database`,`create table`，`create index`，`drop table`，`drop index`，`show`，`insert`，`select`，`delete`，`run`，`exit`这几类子处理流程。 
5.  **异常处理**：所有异常在子处理流程中抛出，在总处理流程中捕获，显示异常对应的状态码和异常信息。 
6.  **循环流程**：处理完一条命令后，重新循环处理下一条命令。直到手动退出，此时关闭输入流并结束程序。此外，若输入流到达末尾 (文件读入)，则关闭输入流并进行程序返回。 

**二级解析流程实现介绍**:

-  `create table`：首先进行正则替换，将`()`和`,`的分割转化为统一形式。接着跳过`create table`关键字，读取表名，然后将属性定义两边的`()`去掉，获得中间属性。得到整个的属性定义字符串后，调用`String`的`split`函数对`,`进行分割，分别得到每个属性的定义。对于每个属性，再次调用`String`的`split`函数对空格进行分割，得到属性名、类型、长度、唯一约束，主键定义等信息。将所有读取的属性信息构造成一个 `Table`类变量，再根据之前读取的表名，调用`API`中的`create_table`函数完成表的创建。此外，流程会对语法错误进行判断，并抛出相应的异常。 
-  `create index`：首先进行正则替换，将`()`的分割转化为统一形式。接着跳过`create index`关键字，读取索引名、然后跳过`on`关键字，读取表名，将属性名两边的`()`去掉，读取属性名。根据读入的索引名、表名、属性名信息，构造一个`Index`类变量，然后调用`API`中的`create_index`函数完成索引的创建。此外，流程会对语法错误进行判断，并抛出相应的异常。 
-  `drop table`：首先跳过`drop table`关键字，读取表名，然后根据表名调用`API`中的`drop_table`函数完成对表的删除。此外，流程会对语法错误进行判断，并抛出相应的异常。 
-  `drop index`：首先跳过`drop index`关键字，读取索引名，然后根据索引名调用`API`中的`drop_index`函数完成对索引的删除。此外，流程会对语法错误进行判断，并抛出相应的异常。 
-  `show`：首先跳过`show`关键字，然后根据下一个词是`indexes`还是`tables`来决定显示索引还是数据表，如果关键词不是这两者则报错。 
-  `insert`：首先进行正则替换，将`()`和`,`的分割转化为统一形式。接着跳过`insert into`关键字，读取表名，然后跳过`values`关键字，将属性值两边的`()`去掉，提取中间的属性值。得到整个属性值定义字符串后，调用`String`类中`split`函数对`,`进行分割，得到每个属性的值。对于每个属性值，判断其两端是否有`''`或`""`，若有则将其去除，将全部得到属性值构造成一个`TableRow`类型的变量，调用`API`中`insert_row`函数对记录进行插入。此外，流程会对语法错误进行判断，并抛出相应的异常。 
-  `select`：`select`语句主要有以下几种形式`select * from [表名];`，`select * from [表名] where [条件];` `select [属性名] from [表名];` 以及 `select [属性名] from [表名] where [条件];`。我们通过自定义函数`String substring(String stmt, String start, String end)`来得到位于两个字符串之间的子串，以最复杂的`select [属性名] from [表名] where [条件];`为例，先通过`substring(stmt,"select ", " from")`来得到属性名或`*`，如果是属性名则通过`split`函数根据`,`进行分割。同理通过`substring ( stmt,"from ", " where")`可以得到表名，通过`substring (stmt,"wuere ", "")`可以得到条件。然后只需根据具体情况判断即可。 
-  `delete`：`delete`的处理方式与`select`类似。 
-  `run`：首先跳过`run`关键字，读取文件名。然后根据文件名构造一个`BufferReader`类的输入流变量，以此为参数递归调用总体解析流程。此外，流程会对对语法错误进行判断，并抛出相应的异常。对于文件不存在等错误，也抛出相应异常。 
-  `exit`：首先关闭`BufferReader`输入流，然后将数据库数据及索引数据持久化并调用`System.out.exit`退出程序。

#### 3.3 _CatalogManager_ 模块

**_CatalogManager_设计思想：**`CatalogManager`负责管理数据库的所有实例（包括数据库中表的数据及索引信息），以及所有索引的Sql语句信息（用于持久化后的索引重建）:

- **bufferManagers**：数据库实例的集合对象，管理所有数据库。
- **bufferManager**：当前使用的数据库对象，根据用户Sql语句切换数据库。
- **indexInfos**：索引信息集合对象，存放所有主键索引信息及用户自定义索引信息。

**_CatalogManager_ 主要函数实现：**

- **初始化**：从D:\\MiniSQL\\data文件读取数据库信息，从D:\\MiniSQL\\index文件读取索引信息。
- **退出**：程序正常关闭时将所有数据信息和索引信息写回到两个文件中。

`CatalogManager`对外提供以下方法：

```java
//启动MiniSql
public static CatalogManager Start() throws MyExceptionHandler{};
//关闭MiniSql
public void Exit() throws MyExceptionHandler{};
//指定数据库
public boolean UseDataBase(String name) throws MyExceptionHandler{};
//创建数据库
public boolean CreateDataBase(String DBName) throws MyExceptionHandler{};
//展示数据库
public void ShowDataBases(){};
//通过字段名获取字段类型
public String GetTypeByName(String table, String field) throws MyExceptionHandler{};
//创建索引
public void CreateIndex(IndexInfo indexInfo) throws MyExceptionHandler{};
//回复索引
public void ResumeIndex(IndexInfo indexInfo) throws MyExceptionHandler{};
//删除索引
public void DropIndex(String Index) throws MyExceptionHandler{};
//建表
public void CreateTable(String tableName, Schema schema) throws MyExceptionHandler{};
//删除表
public void DropTable(String tableName) throws MyExceptionHandler{};
```

#### 3.4 _RecordManager_ 接口

**_RecodManager_ 设计思想：** `RecordManager`负责管理记录表中数据的数据文件，实现最终对文件内记录的增查删改操作，其总体设计思想如下：

-  **单表存储**：一个文件存一张表，一张表对应一个数据页。
-  **文件结构**：采用堆文件结构，记录在有空闲的地方均可插入，无须再对记录顺序进行额外处理。 
-  **空闲空间**：数据页中间为空闲空间，用于存放数据。
-  **记录存储**：记录通过数据页中的记录集和存储。 
-  **文件读写**：使用`FASTJSON`文件块进行读写。 

**_RecordManager_ 主要函数实现：**

-  **建表**：使用Page的构造函数建表，需要指定表明、表元数据等信息。 
-  **删表**：调用`BufferManager`函数将该文件所有在缓冲区的块置为无效。
-  **查找**：不用索引时，对数据集合遍历查找，使用索引时，对索引B+树进行查找。
-  **插入**：根据插入信息向数据集合插入
-  **删除**：对于对应表名的文件，首先读取其第一个块并给其上锁。顺序扫描记录，跳过首字节为`0`的空记录，读取存在记录对应的属性，若符合删除条件则将其标志字节置为`0`，然后将接下来`4Bytes`的空间写入文件头的空闲地址值，用于指示下一个空闲记录地址，最后更新文件头`前4Bytes`的空闲地址，使其指向当前的删除记录所在的地址，完成空闲链表的更新。 

`RecordManager`对外提供如下方法：

```java
//通过条件获得数据
public List<Row> GetRowsByCondition(Condition condition){};
//插入数据
public boolean InsertRecord(Row row) throws MyExceptionHandler{};
//获得字段位置
public int getFieldPos(String fieldName){};
//批量删除数据
public boolean DeleteRows(Condition condition){};
//按条件删除数据
public boolean DeleteRows(List<Condition> conditions, List<String> relations){};
//更新数据
public int UpdateRecord(Row row, Long rowId){};
//查询数据
public Row GetTuple(Long rowId){};
//创建索引
public BPlusTree CreateIndex(IndexInfo indexInfo){};
//删除索引
public void DropIndex(IndexInfo indexInfo) throws MyExceptionHandler{};
```

#### 3.5 _IndexManager_ 接口（stl&zjs）

##### 3.5.1 B+树介绍

###### B+树定义

B+树是一种多路平衡查找树,是对B树(B-Tree)的扩展.
首先,一个M阶的B树的定义为:

- 每个节点最多有M个子节点；
- 每一个非叶子节点（除根节点）至少有ceil(M/2)个子节点；
- 如果根节点不是叶子节点，那么至少有两个子节点；
- 有k个子节点的非叶子节点拥有k-1个键，键按照升序排列；
- 所有叶子节点在同一层；

从定义可以看出来,一个M阶的B树,其叶子节点必须在同一层,每一个节点的子节点数目和键数目都是有规定的.其实不看定义,简单来说,B树是平衡的,而且非叶子节点的子节点是有限制的.最重要的一点是,B树的键是有序的,节点内部的键从左到右依次增大,而且对应的子节点的最小值和最大值在左右两个键之间,这样可以尽可能减少IO次数,提高查询效率.而B+树基本定义与B树相同,不同之处在于:

- 非叶节点仅有索引作用，具体信息均存放在叶节点;
- 树的所有叶子节点构成一个有序链表，可以按照键的排序次序遍历全部记录;

###### B+树查找

从根结点开始，首先从结点内部查找（由于结点内部是升序的，二分查找即可）。比如查找4，结点内部存放1,5,8 那么查到5就可以停了，沿着对应的5左边的指针（区间![](https://www.yuque.com/api/services/graph/generate_redirect/latex?1%5Cleq%20x%3C5#card=math&code=1%5Cleq%20x%3C5&id=jq4t0)）继续向下查找，直到最后进入叶节点。若叶节点中存在该索引值，就能找到对应记录的指针，若不存在，则查找失败。

###### B+树插入

B+树的插入操作相对于查询操作和更新操作来说，会比较复杂，因为当插入的数据而对应新增的Entry让B+树节点容纳不下时（即发生上溢），会触发分裂操作，而分裂操作则会导致生成新的B+树节点，这就需要操作对应的父节点的孩子指针，以满足父节点Entry和孩子指针的有序性，同时如果这个新增的节点会导致这个父节点也上溢的话就需要递归地进行分裂操作。

为了实现这一点，最简单的方法是每个B+树节点都维护一个父指针指向它的父亲，这样当分裂出新的节点时就可以通过这个父指针获取对应的父节点，获取到之后就可以对这个父节点的孩子指针进行相应的操作了。这个方法虽然简单，但是在空间上会有一点额外的损耗，比如在32位机器上，一个指针的大小是4字节，假如一棵B+树有N个节点，那么因为维护整个父指针就会额外损耗 4 * N 字节的空间。

那么有没有什么办法既可以不需要父指针，又可以完成这个分裂操作呢？答案是利用返回值，当子节点分裂出新的节点时，可以将这个节点返回，因为插入操作也是自顶向下按层进行的，所以对应的父节点可以通过返回值拿到这个新增的节点，然后再进行对应的孩子指针的操作就可以了。而如果在插入的时候没有触发分裂操作，这个时候我们只需要返回 null 即可，这样也相当于告诉了父节点，下面没有发生分裂，所以父节点就自然不可能再触发分裂操作了。

###### B+树删除

删除操作仍然使用向兄弟节点借数据和合并相邻节点的方式来进行处理。与插入操作类似，进行删除后可以将删除结果通过返回值的形式返回给父节点，父节点就可以根据这个信息判断自身会不会因此发生下溢从而进行相应的操作。

###### B+树代码接口

```java
//查找B+树上界
public K searchFirst(){};
//查找B+树下界
public K searchLast(){};
//插入节点
public void insert(K entry, E value){};
//等值查询
public List<E> query(K entry){};
//范围查询
public List<E> rangeQuery(K startInclude, K endExclude){};
//更新节点
public boolean update(K entry, E oldValue, E newValue) {};
//删除节点
public boolean remove(K entry, E value){};
//删除节点
public boolean remove(K entry){};
//处理边界
private void handleRootUnderflow(){};
```

##### 3.5.2 _IndexManager_接口

**_IndexManager_设计思想：**`IndexManager`负责管理数据库的索引，利用B+树的对数级查询速度，优化`select`和`delete`的效率，其总体设计思想如下:

- **哈希存储**： 采用`LinkedHashMap`这一数据结构存储所有的索引对应的B+树。`IndexManager`类中维护一个主键索引，三个静态的`LinkedHashMap`示例，分别对应整型 (`Integer`) 索引、字符串 (`String`) 索引和浮点数  (`Float`)  索引的三棵B+树。
- **地址映射**：B+树主键索引key为Integer，Value为对应的数据行。其他索引key为索引键，value为主键，需要进行回表查询。
- **文件读写**：`CatalogManager`读取完`index`信息后，调用`ResumeIndex`函数，从硬盘中读取并建立B+树。

**_IndexManager_主要函数实现：**

- **初始化**：根据`index_catalog`文件中读取到的所有索引信息，从硬盘中分别读取所有索引的B+树，并按键key的类型分别插入到3个哈希表中。
- **插入**：插入时，直接调用B+树的`insert`函数，将键值插入到B+树中。
- **查找**：查找是`IndexManager`的核心，从`Condition`中读取出操作符和比较值后，调用私有方法`satisfies`从树中选取出符合条件的值。`satisfies`使用泛型的技巧，而不是对`Integer`, `String`, `Float`分别写三个函数，节省了代码量。
- **删除**：删除时，直接调用B+树的`delete`函数，将键值从B+树中删除。而在API中，调用删除前，会先调用`select`函数，利用索引优化快速找出要删除元素的`List`，再遍历`List`逐一删除。

```java
//构造方法，指定索引信息
public IndexManager(Schema schema, String dbName, String TableName){};
//条件查询
public List<Row> select(Condition cond) throws IllegalArgumentException{};
//多条件查询
public List<Row> select(List<Condition> conditions, List<String> relations){}; throws IllegalArgumentException
//插入数据
public void insert(Row row) throws IllegalArgumentException{};
//更新数据
public void Update(Row old, Row row){};
//清除索引
public void Clear(List<Row> rows){};
//删除数据
public void Delete(Row row){};
//检查数据
public boolean contains(String name){};
```

#### 3.6 _BufferManager_ 接口

**_Buffer Manager_ 设计思想：** BufferManager代表一个数据库实例，其核心设计方法包括：

-  通过buffer数组管理本数据库的所有数据表，数组最大size为50。
-  通过调用buffer中对应数据页封装好的方法或者自己的方法处理数据页。
- 


`BufferManager`对外提供如下接口：

```java
//构造数据库，指定数据库名字
public BufferManager(String name) throws MyExceptionHandler{};
//建立索引
public BPlusTree CreateIndex(IndexInfo indexInfo) throws MyExceptionHandler{};
//删除索引
public void DropIndex(IndexInfo indexInfo) throws MyExceptionHandler{};
//展示所有数据表
public void ShowTables(){};
//初始化整个数据页buffer
public void InitialBuffer() throws MyExceptionHandler{};
//持久化
public void DestructBufferManager() throws MyExceptionHandler, IOException{};
//数据页写
public boolean WritePageToDisk(int i) throws MyExceptionHandler, IOException{};
//从磁盘文件初始化数据库
public void ConstructBufferManager() throws MyExceptionHandler{};
//读取数据页
public Page ReadPageFromDisk(String tableName) throws MyExceptionHandler {};
//建表
public int CreateTable(String tableName, Schema schema) throws MyExceptionHandler{};
//普通查询
public long SelectTable(String tableName) throws MyExceptionHandler{};
//条件查询
public long SelectTable(String tableName, Condition condition) throws MyExceptionHandler{};
//多条件查询
public long SelectTable(String tableName, List<Condition> conditions, List<String> relations){};
//多条件投影查询
public long SelectTable(String tableName, List<String> names, List<Condition> conditions, List<String> relations) throws MyExceptionHandler{};
//插入
public long InsertIntoTable(String tableName, Row row) throws MyExceptionHandler{};
//删除表
public void DropTable(String tableName) throws MyExceptionHandler{};
//更新数据
public long UpdateTuple(String tableName, List<Condition> datas, Condition condition) throws MyExceptionHandler{};
//多条件更新数据
public long UpdateTuple(String tableName, List<Condition> datas, List<Condition> conditions, List<String> relations) throws MyExceptionHandler{};
//删除数据
public void DeleteTuple(String tableName) throws MyExceptionHandler{};
//单条件删除数据
public void DeleteTuple(String tableName, Condition condition) throws MyExceptionHandler{};
//多条件删除数据
public long DeleteTuple(String tableName, List<Condition> conditions, List<String> relations) throws MyExceptionHandler{};
```

#### 3.7 _DB Files_ 管理

- `CatalogManager`中建立了两个文件`data`和`index`，用于保存所有的`Table`信息和`Index`信息供其他模块查询。`index`并不直接存储B+树结构，而是存储建立索引的信息，在项目启动时再根据所有信息重建索引。在`CatalogManager`中`Index`的存储是一体的，没有根据不同表分开存储`Index`。

## 4 系统测试

#### 4.1 程序的启动

我们将_MiniSQL_的代码编译打包为.jar文件，进入系统控制台cd到目录下后，输入指令`java -jar .\MiniSQL2.0-1.0-SNAPSHOT.jar`即可启动程序，界面如下：

![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686640730343-3cd359b9-176f-4f35-9287-dfaa3f793583.png#averageHue=%23101010&clientId=uba076438-304f-4&from=paste&height=573&id=u8a04cfdb&originHeight=945&originWidth=1727&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=49513&status=done&style=none&taskId=u1e10cdde-03d6-49b9-8452-97930883b0d&title=&width=1046.6666061709661)
![](assets/1560500012132.png#id=JRGoD&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
#### 4.2 表的建立与测试数据的插入

首先，我们建立数据库`db01`并指定使用该数据库：
```
create database db01;
use database db01;
show databases;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641137118-87f01289-5b94-4ea9-a70c-2baef1bea983.png#averageHue=%23222222&clientId=uba076438-304f-4&from=paste&height=72&id=u538519a4&originHeight=119&originWidth=446&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=11014&status=done&style=none&taskId=u63973247-5bfb-4b6d-b6b9-fc1987992f0&title=&width=270.3030146799368)

我们建立表`account`：

```
create table account(
  id int, 
  name char(16) unique, 
  balance float, 
  primary key(id)
);
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641154733-1db28398-267c-4199-8e90-ff8ea5319033.png#averageHue=%231a1a1a&clientId=uba076438-304f-4&from=paste&height=127&id=ub5e0df37&originHeight=210&originWidth=471&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=15788&status=done&style=none&taskId=u222cf2cb-7fb5-41f5-b716-fb5a12c652e&title=&width=285.454528955718)

然后插入10000条测试数据。由于数据量较大，我们使用`run`命令进行文件批处理：

```
run D:\MiniSQL\account00.txt;
```

脚本文件account00.txt内容如下：

```
insert into account values(12500000, "name0", 514.35);
insert into account values(12500001, "name1", 103.14);
insert into account values(12500002, "name2", 981.86);
insert into account values(12500003, "name3", 926.51);
insert into account values(12500004, "name4", 4.87);
insert into account values(12500005, "name5", 437.08);
insert into account values(12500006, "name6", 373.75);
insert into account values(12500007, "name7", 681.87);
insert into account values(12500008, "name8", 666.64);
insert into account values(12500009, "name9", 67.46);
......
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641165820-82a26077-80b6-4e75-aecc-179133fa30f2.png#averageHue=%23262626&clientId=uba076438-304f-4&from=paste&height=22&id=uabba5ef9&originHeight=37&originWidth=552&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=4475&status=done&style=none&taskId=u4c7464e1-72ca-4586-9f9e-639d45fd846&title=&width=334.54543520924915)

#### 4.3 多条测试语句

1.  首先考察int类型上的等值条件查询 
```
select * from account where id = 12509994;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641603772-4b561266-3e57-4449-a666-8e872f580906.png#averageHue=%231d1d1d&clientId=uba076438-304f-4&from=paste&height=107&id=u7d909370&originHeight=176&originWidth=735&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16745&status=done&style=none&taskId=ue1225756-b6f3-437e-b22b-2c6033b5525&title=&width=445.4545197079676)
![](assets/1560500813851.png#id=itCQm&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

2.  考察float类型上的等值条件查询 
```
select * from account where balance = 67.46;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641618907-01730202-532f-4a84-99aa-61c6110fa796.png#averageHue=%231c1c1c&clientId=uba076438-304f-4&from=paste&height=107&id=u2d330c19&originHeight=176&originWidth=776&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16307&status=done&style=none&taskId=u52f9e7dc-b368-4974-9a38-55d9445c106&title=&width=470.3030031202488)
![](assets/1560500852296.png#id=OJhbt&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

3.  考察char类型上的等值条件查询，此处需观察执行时间`t1` 
```
select * from account where name = "name9999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642961544-6ff6ca03-8198-4e8d-90c2-df576a8b1cc9.png#averageHue=%231a1a1a&clientId=uba076438-304f-4&from=paste&height=110&id=uf39faaac&originHeight=182&originWidth=838&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16369&status=done&style=none&taskId=uc111b0e2-1c05-4bc4-a4eb-cac5cff1b86&title=&width=507.8787585241862)
![](assets/1560500883478.png#id=WB0tB&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
记录：`t1`=0.006s 

4.  考察int类型上的不等条件查询，观察数量 
```
select * from account where id <> 12500004;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686641658938-02d95ef9-1038-4ed1-bb05-89e94a106618.png#averageHue=%23202020&clientId=uba076438-304f-4&from=paste&height=38&id=uc664b1bd&originHeight=62&originWidth=660&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=6276&status=done&style=none&taskId=u16beae2a-ed40-4acf-a357-19e9beda4f3&title=&width=399.999976880624)
数量为9999，符合预期 

5.  考察float类型上的不等条件查询，观察数量 
```
select * from account where balance <> 67.46;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642077723-a873231f-de60-42e6-8945-5cd1eaf2f046.png#averageHue=%231f1f1f&clientId=uba076438-304f-4&from=paste&height=38&id=u19c99a45&originHeight=62&originWidth=670&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=5974&status=done&style=none&taskId=ud90957dc-f977-4780-bcf1-944dc2d0a8d&title=&width=406.06058259093646)
数量为9999，与2中对比知，由于 ![](https://cdn.nlark.com/yuque/__latex/23659e4f6e96b1b99b108e17f1bcc08d.svg#card=math&code=9999%20%3D%2010000%20-%201&id=LX6SR)，符合预期 

6.  考察char类型上的不等条件查询，观察数量 
```
select * from account where name <> "name9999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642127593-2de53035-ab59-49d0-b339-1e2eab545ef4.png#averageHue=%231f1f1f&clientId=uba076438-304f-4&from=paste&height=38&id=ubee66174&originHeight=62&originWidth=684&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=6079&status=done&style=none&taskId=ud6abc3c1-231c-401f-a55f-94a9770db07&title=&width=414.54543058537394)
数量为9999，由于name是unique key，因此符合预期 

7.  考察多条件and查询，观察数量 
```
select * from account where balance >= 999.35 and balance < 1000.0;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642224748-78d462dc-7042-4b66-9f8b-4b8cda9bc15f.png#averageHue=%23181818&clientId=uba076438-304f-4&from=paste&height=212&id=uef3cf170&originHeight=350&originWidth=1080&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=37601&status=done&style=none&taskId=ufd7e623d-0129-4023-9ddf-2a191e30180&title=&width=654.5454167137483)
可以看到，选取出来的数据balance的确处于![](https://cdn.nlark.com/yuque/__latex/de4ca00446f5d5dd573dd4c67596b66e.svg#card=math&code=%28999.35%2C1000.0%29&id=jetsK)之间 

8.  考察多条件and查询，观察数量 
```
select * from account where id < 12505000 and name > "name5990";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642255842-2deb4bcb-b320-425b-b5d3-7fe5413f0992.png#averageHue=%231f1f1f&clientId=uba076438-304f-4&from=paste&height=37&id=u688af4c7&originHeight=61&originWidth=670&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=5734&status=done&style=none&taskId=u7f28b3f1-85f2-40ee-ac4a-45383f478d8&title=&width=406.06058259093646)
![](assets/1560501200973.png#id=QUQOK&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

9.  考察unique key约束冲突 
```
insert into account values(12600000, name9999, 123);
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642339026-f34263ec-2527-4abc-a076-f361dfb4a47a.png#averageHue=%23252525&clientId=uba076438-304f-4&from=paste&height=41&id=ud9d3a2fd&originHeight=68&originWidth=900&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=13164&status=done&style=none&taskId=ube4ca639-31bc-4b33-b396-17d380ad33b&title=&width=545.4545139281236)
由于表中已存在`name`=name9999的数据，因此报错 

10.  考察索引的建立 
   -  考察非unique key建立索引的报错 
```
create index idx01 on account(balance);
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642871931-9588640a-b957-4d2a-b017-227928c56d4e.png#averageHue=%23262626&clientId=uba076438-304f-4&from=paste&height=41&id=ua708fcb5&originHeight=67&originWidth=839&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=12130&status=done&style=none&taskId=u4923991d-ac5f-4f29-bfad-fd92bde8543&title=&width=508.48481909521746)
![](assets/1560501284020.png#id=QoQnt&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
由于unique key才能建立索引，因此报错 

   -  在name这个unique属性上创建index 
```
create index idx01 on account(name);
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642889783-9733ebc1-a15e-4fdd-bf15-953545ec5e6c.png#averageHue=%23252525&clientId=uba076438-304f-4&from=paste&height=22&id=ud6b9ed33&originHeight=36&originWidth=660&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=4899&status=done&style=none&taskId=u6319802e-290e-4c48-9256-c857a393b4d&title=&width=399.999976880624)
索引创建成功 

11.  此处需观察执行时间`t2` 
```
select * from account where name = "name9999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686642991384-03708c66-3014-4264-b246-df9766f8d251.png#averageHue=%231b1b1b&clientId=uba076438-304f-4&from=paste&height=110&id=ubace0c90&originHeight=182&originWidth=797&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16097&status=done&style=none&taskId=uad7d01a9-6330-40a1-8e1f-e65f682c86b&title=&width=483.030275111905)

建立索引后，`t2`=0.001s，与建立索引前的`t1`=0.006s相比，速度提升明显 

12.  考察在建立索引后再插入数据 
```
insert into account values(12600000, name19999, 123.00);
```

![](assets/1560501583505.png#id=pQ8gj&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

13.  考察是否插入成功，并需观察执行时间`t3` 
```
select * from account where name = "name19999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643243260-d983c948-5759-48c6-9c3b-6c550390ae5a.png#averageHue=%231c1c1c&clientId=uba076438-304f-4&from=paste&height=105&id=u5d104d5f&originHeight=174&originWidth=799&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=15911&status=done&style=none&taskId=u57c6ec56-7228-420c-9d81-dfb6112eff2&title=&width=484.2423962539675)
插入成功，`t3`=0.000s 

14.  考察delete 
```
delete from account where name = "name5678";
```

![](assets/1560501657513.png#id=uKcau&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

15.  考察是否删除成功 
```
select from account where name = "name5678";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643335273-1d23af4a-a63e-4bd9-bdce-d55fba246068.png#averageHue=%231d1d1d&clientId=uba076438-304f-4&from=paste&height=105&id=u9421dde5&originHeight=174&originWidth=753&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16412&status=done&style=none&taskId=u04131a70-2a28-4f0c-b3bd-df1390b74d4&title=&width=456.3636099865301)
可见，删除成功 

16.  重新插入 
```
insert into account values(12505678, "name5678", 235.94);
```

![](assets/1560501583505.png#id=PX3cZ&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

17.  考察drop index 
```
drop index idx01;
```

![](assets/1560501742658.png#id=bn7QB&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

18.  需观察此处的执行时间`t4` 
```
select * from account where name = "name9999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643458607-e348c327-4f78-4885-a69e-a82b1198de04.png#averageHue=%231b1b1b&clientId=uba076438-304f-4&from=paste&height=109&id=u1ed28e69&originHeight=180&originWidth=801&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16242&status=done&style=none&taskId=uaf3f75d9-8019-4803-aba0-13910bb31cd&title=&width=485.45451739603)
与`t3`比较发现，删除索引后，查找速度的确明显变慢 

19.  需观察此处的执行时间`t5` 
```
select * from account where name = "name19999";
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643549680-e784ba9f-0a5f-4c46-864e-972573432ad7.png#averageHue=%231b1b1b&clientId=uba076438-304f-4&from=paste&height=110&id=ue0af2fd3&originHeight=181&originWidth=820&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=16027&status=done&style=none&taskId=u8a1c4861-9e7b-4530-b632-e73d8a8f0b3&title=&width=496.9696682456237)
与`t2`比较发现，删除索引后，查找速度的确明显变慢 

20.  考察主键（有索引）delete 
```
delete from account where id = 12500000;
```

![](assets/1560502001954.png#id=SkfTL&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=) 

21.  考察是否删除成功 
```
select * from account where id = 12500000;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643657346-c76682c3-09fb-4605-a6b7-292b4d1de961.png#averageHue=%231b1b1b&clientId=uba076438-304f-4&from=paste&height=127&id=ue041e483&originHeight=209&originWidth=814&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=19691&status=done&style=none&taskId=u46411737-0fe2-4f0a-aa56-3a0322e2734&title=&width=493.33330481943625)
可见，删除成功 

22.  考察（无索引）delete 
```
delete from account where balance = 67.46;
```

23.  考察是否删除成功 
```
select * from student2 where score=98.5;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643738299-3389ab9b-a01a-4c3a-9974-6b578f2f43c9.png#averageHue=%231e1e1e&clientId=uba076438-304f-4&from=paste&height=107&id=bzOCg&originHeight=176&originWidth=775&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=18188&status=done&style=none&taskId=uba1b9e7b-7838-4c79-8ae1-9f9fa316734&title=&width=469.69694254921757)
可见，删除成功 

24.  考察delete（所有元素） 
```
delete from account;
```

25.  考察是否删除成功 
```
select * from account;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643798917-ad54077a-ddfc-416a-829b-943ee0eb9e4a.png#averageHue=%231c1c1c&clientId=uba076438-304f-4&from=paste&height=143&id=ub8b6b250&originHeight=236&originWidth=680&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=22464&status=done&style=none&taskId=ucf596a9f-257d-43d3-a53b-586763fcd21&title=&width=412.12118830124894)
可见，删除成功 

26.  考察drop table 
```
drop table account;
```

![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686643949040-c2ca6bef-dbc6-4b28-830d-a8cd529f8bfd.png#averageHue=%231e1e1e&clientId=uba076438-304f-4&from=paste&height=144&id=ua193b539&originHeight=238&originWidth=425&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=19798&status=done&style=none&taskId=u2b603b68-8c33-47bc-8f84-522ee094424&title=&width=257.57574268828057)

27.  考察drop table后再select是否报错 
```
select * from account;
```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686644066737-1259c06e-4fe8-417f-9ffe-f35b22ec6dd3.png#averageHue=%231d1d1d&clientId=uba076438-304f-4&from=paste&height=110&id=u170615ee&originHeight=182&originWidth=660&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=21491&status=done&style=none&taskId=u7cb2db1d-77b9-4701-832d-16a80b91378&title=&width=399.999976880624)
![](assets/1560502250898.png#id=qYJuS&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
表account已不存在，因此select报错。 

#### 4.4 程序结束

结束程序时，应输入语句`exit`（而不是直接点击右上角的红叉），因为退出时会执行一些终止化操作，把_CatalogManager_、_IndexManager_ 中的信息以及 _BufferManager_ 中的缓存写回硬盘，直接点击红叉可能会导致下次程序启动时初始化错误。

退出后，目录中多出了data, index两个文件：
![image.png](https://cdn.nlark.com/yuque/0/2023/png/35639742/1686644759290-648de0d2-0c13-4a8e-8042-59d35de0a0c6.png#averageHue=%23f7f7f6&clientId=uba076438-304f-4&from=paste&height=622&id=ua8102c59&originHeight=1027&originWidth=1681&originalType=binary&ratio=1.6500000953674316&rotation=0&showTitle=false&size=79449&status=done&style=none&taskId=u29a71f46-21f2-4ddf-b54a-8ae2b953f6c&title=&width=1018.7878199035287)
![](assets/1560502736677.png#id=Y3v0M&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
#### 4.5 结论

通过多条语句的测试，以及对一些select语句选取到的数量的观察可知，_MiniSQL_能够很好地完成指定的功能。而对于一些逻辑错误（如unique key重复插入、非unique key建索引等）也能及时检测并报错。而在索引的性能对比中，在有无索引情况下执行相同语句，有`t2`<`t1`, `t2`<`t5`和`t3`<`t4`，索引对性能的提升一目了然。
当然，本项目也存在一些缺点，其中最致命的是对用户的输入语句语法检查不够强大，有可能在用户输入某些非法命令时程序异常退出。

## 5 其他说明

#### 项目Git地址

开源项目地址： [https://github.com/LTLVL/MiniSQL2.0.git](https://github.com/LTLVL/MiniSQL2.0.git)
#### 

