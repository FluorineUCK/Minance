# Minance

Minance 是一个独立金融模拟 NeoForge 模组。当前目标是在 Minecraft 世界里把村民、村庄、商品供需、公司股权、指数、衍生品、基金和结构化产品串成一个可持久化的市场系统，并通过 LDLib2 面板展示市场状态。

## 基本信息

- Minecraft: `1.21.1`
- NeoForge: `21.1.220`
- Java: `21`
- Mod ID: `minance`
- 依赖: `ldlib2 >= 2.2.18`
- 本地依赖位置: `comp_mod/ldlib2-neoforge-1.21.1-2.2.18-all.jar`

## 当前功能

### 数据驱动规则

配置和规则分为两层：

- `src/main/resources/data/minance/config/*.json`: 数值配置、阈值、权重、节奏和模型参数，目前包含 `commodity`、`company`、`economy`、`finance`、`market`、`risk`、`trading`。`market.index.indices` 可配置指数成分匹配、权重方法、再构成和再平衡节奏；`finance.fund` 可配置指数跟踪基金初始配置比例和申购/赎回溢折价阈值。
- `src/main/resources/data/minance/minance_rules/company_names/default.json`: 村庄公司的名称词库。
- `src/main/resources/data/minance/minance_rules/professions/*.json`: 村民职业的预期股份、生产商品、需求商品、风险偏好和交易节奏。

这些数据由 `ConfigReloadListener`、`CodecRuleReloadListener` 和 `MinanceReloaders` 注册，支持按数据包路径维护。

### 村庄公司

`VillageCompanyService` 会扫描已加载村庄，按钟的位置聚合村民并形成 `VillageCandidate`。候选公司会根据职业规则累积资金，达到注册门槛后转为 `VillageCompany`。

公司注册资本公式：

```text
required_capital = minimum_capital_per_share * sum(expected_shares from villager professions)
```

注册后的公司会记录：

- 公司 ID、名称、维度和钟坐标。
- 村民职业构成。
- 总股数、股东持仓和公司资金。
- 股价、价格 K 线和财务报告。
- 来自高级村民的非现货需求压力。

### 商品现货

`SpotMarketService` 维护每种商品的供给、需求、库存、价格、成交量和波动率。现货价格由库存压力、资金流、稳定器和配置里的微观结构参数共同驱动。

`CommodityStabilizationDesk` 会在供需偏离时创建反向流量，用于减缓价格过度偏离。

### 金融市场引擎

`FinancialMarketEngine` 为股权、期货、期权、基金、债券和结构化产品提供统一的价格更新逻辑。它维护订单流动性表面，并用多类交易者行为生成买卖压力：

- retail noise
- market maker
- anchoring
- momentum
- mean reversion
- value investor
- herding
- panic
- arbitrageur
- fund manager

### 产品线

当前源码中已经拆出这些金融产品模块：

- `product/commodity/spot`: 商品现货市场。
- `product/commodity/core`: 商品身份、属性和物理库存状态。
- `product/component`: 通用金融产品组件集合，当前覆盖 fund、structured、future、option；规划新增 `core` 属性叠加层和 `collection` 集合索引。
- `product/component/derivative`: 当前承载商品期货、期权、合约状态、交割方式和多层衍生品展开；规划拆分为 `product/component/derivative/future` 和 `product/component/derivative/option`。
- `product/equity`: 村庄公司股权资产和可交易股权同步。
- `market/index`: `food_index`、`crops_index`、`minerals_index`、`tools_index`，并支持配置化权重方法、成分再构成和再平衡节奏。
- `product/component/fund`: 基金状态、持仓、指数跟踪基金创建、NAV/跟踪误差和申购/赎回信号。
- `product/component/structured`: 结构化产品状态和受益人声明。

### 持久化

`MinanceSavedData` 会把市场状态写入世界存档，包含：

- 村庄候选公司和已注册公司。
- 现货资产。
- 商品期货、期权和合约。
- 市场指数。
- 金融市场引擎状态。
- 基金和结构化产品。

保存键名为 `minance_world_market`。

### 客户端面板

默认按 `M` 打开 Minance 市场面板，可在 Controls > Key Binds > Minance 中重新绑定。

当前客户端面板应理解为默认公共金融机构 `central_bank_and_securities` 的服务终端。它代表一个合并的中央银行与证券服务主体，提供公共市场查看、证券/衍生品/基金/结构化产品入口、指数查看、订单流和调试信息。面板本身不是市场权威逻辑，不负责定价、授信、清算、结算或产品状态持久化。当前快捷键入口会绑定到 `FinancialServiceProviderContext` 中的默认公共服务主体，未来方块或菜单入口可以传入玩家自建机构上下文。

客户端 UI 使用 LDLib2：

- `MarketLdLibDashboard` 加载 `assets/minance/market_dashboard.xml`。
- 加载失败时会回退到代码创建的 `MarketPanelElement`。
- 面板包含公司/候选公司、商品、指数、衍生品、订单流、产品详情和调试信息。
- 图表元素支持 K 线、柱状图、成交量、OHLC 和移动均线等市场视图。

后续玩家应能通过方块、菜单、权限或公司/机构所有权创建自己的金融服务入口。玩家自建机构规划由 `PlayerInstitutionPlanningService` 表示，可记录所有者、操作员权限、服务授权和牌照，并转换为同一类 `FinancialServiceProviderContext`。玩家自建机构应复用同一批市场、产品、清算、授信和托管服务接口，而不是复制一套 UI 内逻辑。

## 命令

命令根节点是 `/market`，需要 2 级权限。

```text
/market companies
/market candidates
/market company <company_id>

/market commodities
/market commodity <item_id>

/market equities
/market indices

/market derivatives
/market derivative <product_id>
/market derivatives expand <product_id>

/market funds
/market funds list
/market funds create_index <fund_id> <index_id> <cash> <shares>
/market funds company give <company_id> <amount>
/market funds company remove <company_id> <amount>

/market structured
/market debug
/market provider
/market ui
```

## 项目结构

一级目录的详细职责边界见 [docs/package-boundaries.md](docs/package-boundaries.md)。核心规则是：

- `entity/` 记录 Minecraft 世界里的经济主体和原始事实。
- `product/` 记录可交易或可估值产品的状态、生命周期和产品专属信号。
- `market/` 记录产品无关的市场机制、指数、流动性和价格压力聚合。
- `data/` 负责加载、重载和持久化数据，不承载定价公式。
- `rule/` 负责数据包规则模型和注册表，不承载运行时可变状态。
- `config/` 负责 Java 侧配置类型和注册，不承载数据包内容规则。
- `client/`、`command/`、`network/` 是外层适配，不作为权威模拟逻辑位置。
- 二级目录按领域切片命名；`core/` 只表示同一父包下兄弟目录共享的基础，不表示全局公共层。
- 非单项功能需要拆成独立源码文件，并归入最近的领域文件夹；避免继续扩大长文件和高耦合类。
- 每个功能必须有统一调用入口，并在对应二级目录的 `CALLING.md` 中记录调用方式、调用方、生命周期、输入输出和禁止绕过项。

```text
src/main/java/com/fluorineuck/minance/
|-- api/                 # 预留的稳定扩展 API、DTO 和集成契约
|-- client/              # LDLib2 市场面板、图表、订单流和产品详情 UI
|-- command/             # /market 调试和管理命令
|-- config/              # Java 侧配置类型、默认值和配置注册
|-- data/                # 数据包/配置加载器、重载器和世界存档边界
|-- entity/company/      # 村庄公司、候选公司、价格条和财务报告
|-- entity/institution/  # 金融机构身份、角色授权、玩家机构规划、服务主体上下文和机构来源信号入口
|-- entity/player/       # 玩家经济身份和未来玩家行为入口
|-- entity/village/      # 已加载村庄扫描和候选公司聚合
|-- entity/villager/     # 村民交易发现和市场行为入口
|-- entity/wandering_merchant/ # 流浪商人经济身份和未来供需事件入口
|-- market/event/        # 规范化市场事件和未来事件路由
|-- market/financial/    # 金融微观结构和统一价格引擎
|-- market/index/        # 商品指数
|-- network/             # 包、同步消息和客户端/服务端状态传输
|-- product/commodity/core/ # 商品身份、属性和物理库存
|-- product/commodity/spot/ # 商品现货事件、流量、结算和价格历史
|-- product/component/   # 通用金融产品组件集合：fund、structured、derivative，目标拆出 core 与 collection
|-- product/equity/      # 公司股权资产
|-- product/insurance/   # 保险产品预留目录
|-- product/liabilities/ # 债务、负债和信用产品
|-- rule/                # 数据驱动规则模型
+-- Minance.java         # 模组入口和事件注册
```

资源目录：

```text
src/main/resources/
|-- META-INF/neoforge.mods.toml
|-- assets/minance/lang/en_us.json
|-- assets/minance/market_dashboard.xml
|-- data/minance/config/
+-- data/minance/minance_rules/
```

## 构建和运行

在 Windows PowerShell 中：

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

常用开发入口：

- `build.gradle`: NeoForge、Java toolchain、LDLib2 本地 jar 和 JUnit 配置。
- `settings.gradle`: Gradle 插件仓库和项目名。
- `gradle.properties`: Gradle JVM、daemon、parallel 和 cache 设置。
- `.vscode/launch.json`: VS Code 运行配置。

## 后续开发方向

- 补齐 `network` 同步层，让服务端市场状态更明确地同步到客户端面板。
- 增加玩家可使用的入口物品、方块或菜单，而不是只依赖快捷键和命令。
- 将当前市场面板明确为默认 `central_bank_and_securities` 服务终端，同时保留玩家自建交易所、经纪、做市、资产管理、授信、存款/贷款、衍生品、清算/托管和保险服务的路径。
- 将 `liabilities`、`insurance`、`structured` 等预留产品线继续产品化，先补齐信用产品、存款产品、保险/担保产品分类。
- 将信用从实体标签或通用属性改为银行、证券公司或评级服务给出的评估/授信/利差报价关系。
- 为 `product/component` 添加 `core` 属性叠加层，用统一组件属性给具体产品叠加底层暴露、期限、结算、NAV 锚、杠杆等描述。
- 将期货和期权从当前 `product/component/derivative` 混合实现拆成 `future` 与 `option` 两个实现目录。
- 为现货、公司注册、衍生品展开和基金估值补测试。
- 继续把村民投资压力转化为真实订单、合约和持仓行为。
