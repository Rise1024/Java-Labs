---
title: Spring MVC 核心架构详解
description: Spring MVC 核心架构详解
tags: [Spring MVC, Web]
category: Spring
date: 2025-09-25
---

# Spring MVC 核心架构详解

## 🎯 概述

Spring MVC 是 Spring 框架中用于构建 Web 应用程序的模块，它基于模型-视图-控制器（MVC）设计模式。Spring MVC 提供了一个强大而灵活的 Web 框架，支持多种视图技术、RESTful Web 服务、以及现代 Web 应用开发的各种需求。根据 [Spring Framework 官方文档](https://docs.spring.io/spring-framework/reference/web.html)，Spring MVC 构建在 Servlet API 之上，为 Web 应用开发提供了完整的解决方案。

## 🏗️ Spring MVC 架构概览

### 核心组件架构

```
客户端请求 (Client Request)
    ↓
前端控制器 (DispatcherServlet)
    ↓
处理器映射 (HandlerMapping)
    ↓
处理器适配器 (HandlerAdapter)
    ↓
控制器 (Controller)
    ↓
模型和视图 (ModelAndView)
    ↓
视图解析器 (ViewResolver)
    ↓
视图 (View)
    ↓
响应 (Response)
```

### 组件关系图

```
┌─────────────────────────────────────────────────────┐
│                  DispatcherServlet                  │
│  ┌───────────────┐  ┌───────────────┐              │
│  │ HandlerMapping│  │ViewResolver   │              │
│  └───────────────┘  └───────────────┘              │
│  ┌───────────────┐  ┌───────────────┐              │
│  │HandlerAdapter │  │View           │              │
│  └───────────────┘  └───────────────┘              │
└─────────────────────────────────────────────────────┘
             │                      │
             ▼                      ▼
    ┌──────────────┐       ┌──────────────┐
    │ Controller   │       │    Model     │
    └──────────────┘       └──────────────┘
```

## 🚀 DispatcherServlet 核心机制

### 1. DispatcherServlet 初始化

```java
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "com.example.web")
public class WebConfig implements WebMvcConfigurer {

    // DispatcherServlet 配置
    @Bean
    public DispatcherServlet dispatcherServlet() {
        DispatcherServlet servlet = new DispatcherServlet();
        servlet.setContextClass(AnnotationConfigWebApplicationContext.class);
        servlet.setContextConfigLocation("com.example.config.WebConfig");
        return servlet;
    }

    // Servlet 注册
    @Bean
    public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration() {
        ServletRegistrationBean<DispatcherServlet> registration = 
            new ServletRegistrationBean<>(dispatcherServlet(), "/");
        registration.setName("dispatcher");
        registration.setLoadOnStartup(1);
        return registration;
    }
}
```

### 2. 请求处理流程详解

```java
// DispatcherServlet 核心处理逻辑（简化版）
public class DispatcherServletFlow {

    public void doDispatch(HttpServletRequest request, HttpServletResponse response) {
        HandlerExecutionChain mappedHandler = null;
        ModelAndView mv = null;

        try {
            // 1. 检查是否为文件上传请求
            processedRequest = checkMultipart(request);

            // 2. 获取处理器映射
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                noHandlerFound(processedRequest, response);
                return;
            }

            // 3. 获取处理器适配器
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

            // 4. 处理 Last-Modified 头
            String method = request.getMethod();
            boolean isGet = "GET".equals(method);
            if (isGet || "HEAD".equals(method)) {
                long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                if (isGet && isLastModifiedRequest(request, lastModified)) {
                    response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                    return;
                }
            }

            // 5. 执行拦截器预处理
            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }

            // 6. 实际处理请求
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

            // 7. 设置默认视图名
            if (mv != null && !mv.hasView()) {
                String defaultViewName = getDefaultViewName(request);
                if (defaultViewName != null) {
                    mv.setViewName(defaultViewName);
                }
            }

            // 8. 执行拦截器后处理
            mappedHandler.applyPostHandle(processedRequest, response, mv);

        } catch (Exception ex) {
            // 异常处理
            handleException(processedRequest, response, mappedHandler, ex);
        }

        // 9. 处理分发结果（渲染视图）
        processDispatchResult(processedRequest, response, mappedHandler, mv, null);
    }
}
```

### 3. Spring Boot 中的自动配置

```java
// Spring Boot 自动配置 DispatcherServlet
@Configuration
@EnableWebMvc
public class SpringBootWebConfig {

    // Spring Boot 会自动配置以下组件：

    // 1. DispatcherServlet 自动注册
    @Bean
    @ConditionalOnMissingBean
    public DispatcherServletRegistrationBean dispatcherServletRegistration(
            DispatcherServlet dispatcherServlet,
            WebMvcProperties webMvcProperties,
            ObjectProvider<MultipartConfigElement> multipartConfig) {
        
        DispatcherServletRegistrationBean registration = 
            new DispatcherServletRegistrationBean(dispatcherServlet, webMvcProperties.getServlet().getPath());
        
        registration.setName("dispatcherServlet");
        registration.setLoadOnStartup(webMvcProperties.getServlet().getLoadOnStartup());
        multipartConfig.ifAvailable(registration::setMultipartConfig);
        
        return registration;
    }

    // 2. 默认错误页面处理
    @Bean
    @ConditionalOnMissingBean(name = "error")
    public View defaultErrorView() {
        return new StaticView();
    }

    // 3. 字符编码过滤器
    @Bean
    @ConditionalOnMissingBean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }
}
```

## 🎯 控制器开发详解

### 1. 基础控制器模式

```java
@Controller
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET 请求处理
    @GetMapping
    public String listUsers(Model model,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        Page<User> users = userService.findUsers(PageRequest.of(page, size));
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        return "users/list"; // 返回视图名
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "users/detail";
    }

    // 创建用户表单
    @GetMapping("/create")
    public String createUserForm(Model model) {
        model.addAttribute("user", new User());
        return "users/create";
    }

    // 处理创建用户
    @PostMapping
    public String createUser(@Valid @ModelAttribute User user,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "users/create";
        }

        User savedUser = userService.save(user);
        redirectAttributes.addFlashAttribute("message", "用户创建成功");
        return "redirect:/api/users/" + savedUser.getId();
    }

    // 更新用户
    @PutMapping("/{id}")
    public String updateUser(@PathVariable Long id,
                           @Valid @ModelAttribute User user,
                           BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "users/edit";
        }

        user.setId(id);
        userService.save(user);
        return "redirect:/api/users/" + id;
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id,
                           RedirectAttributes redirectAttributes) {
        userService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "用户删除成功");
        return "redirect:/api/users";
    }
}
```

### 2. RESTful API 控制器

```java
@RestController
@RequestMapping("/api/v1/users")
@Validated
public class UserRestController {

    private final UserService userService;
    private final UserDTOMapper userMapper;

    public UserRestController(UserService userService, UserDTOMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    // 获取用户列表（支持分页和排序）
    @GetMapping
    public ResponseEntity<Page<UserDTO>> getUsers(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String search) {

        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) 
            ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<User> users = StringUtils.hasText(search) 
            ? userService.searchUsers(search, pageable)
            : userService.findAll(pageable);

        Page<UserDTO> userDTOs = users.map(userMapper::toDTO);
        
        return ResponseEntity.ok(userDTOs);
    }

    // 获取单个用户
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable @Positive Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(userMapper.toDTO(user));
    }

    // 创建用户
    @PostMapping
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request,
                                            UriComponentsBuilder uriBuilder) {
        User user = userMapper.toEntity(request);
        User savedUser = userService.save(user);
        UserDTO userDTO = userMapper.toDTO(savedUser);

        URI location = uriBuilder.path("/api/v1/users/{id}")
            .buildAndExpand(savedUser.getId())
            .toUri();

        return ResponseEntity.created(location).body(userDTO);
    }

    // 更新用户
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable @Positive Long id,
                                            @Valid @RequestBody UpdateUserRequest request) {
        User existingUser = userService.findById(id);
        userMapper.updateEntity(request, existingUser);
        User updatedUser = userService.save(existingUser);
        
        return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    }

    // 部分更新用户
    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> patchUser(@PathVariable @Positive Long id,
                                           @RequestBody Map<String, Object> updates) {
        User user = userService.findById(id);
        userService.patchUser(user, updates);
        User updatedUser = userService.save(user);
        
        return ResponseEntity.ok(userMapper.toDTO(updatedUser));
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @Positive Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 批量操作
    @PostMapping("/batch")
    public ResponseEntity<BatchOperationResult> batchOperations(
            @Valid @RequestBody BatchUserRequest request) {
        
        BatchOperationResult result = userService.batchOperation(request);
        return ResponseEntity.ok(result);
    }

    // 用户统计信息
    @GetMapping("/statistics")
    public ResponseEntity<UserStatistics> getUserStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        
        UserStatistics statistics = userService.getStatistics(from, to);
        return ResponseEntity.ok(statistics);
    }
}
```

### 3. 请求参数处理

```java
@RestController
@RequestMapping("/api/examples")
public class ParameterHandlingController {

    // 路径变量
    @GetMapping("/users/{userId}/orders/{orderId}")
    public ResponseEntity<OrderDTO> getOrderByUserAndId(
            @PathVariable("userId") Long userId,
            @PathVariable("orderId") Long orderId) {
        // 处理逻辑
        return ResponseEntity.ok(new OrderDTO());
    }

    // 请求参数
    @GetMapping("/search")
    public ResponseEntity<List<UserDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "name") String searchField,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        // 搜索逻辑
        return ResponseEntity.ok(new ArrayList<>());
    }

    // 请求头处理
    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(
            @RequestHeader("Content-Type") String contentType,
            @RequestHeader(value = "X-Upload-Size", required = false) Long uploadSize,
            @RequestHeader Map<String, String> headers,
            @RequestBody byte[] fileData) {
        
        System.out.println("Content-Type: " + contentType);
        System.out.println("Upload-Size: " + uploadSize);
        System.out.println("All headers: " + headers);
        
        return ResponseEntity.ok("文件上传成功");
    }

    // Cookie 处理
    @GetMapping("/preferences")
    public ResponseEntity<String> getUserPreferences(
            @CookieValue(value = "theme", defaultValue = "light") String theme,
            @CookieValue(value = "language", defaultValue = "zh-CN") String language) {
        
        return ResponseEntity.ok("主题: " + theme + ", 语言: " + language);
    }

    // 矩阵变量
    @GetMapping("/cars/{brand;color=red,blue}/models/{model}")
    public ResponseEntity<String> getCarInfo(
            @PathVariable String brand,
            @MatrixVariable(pathVar = "brand") String color,
            @PathVariable String model) {
        
        return ResponseEntity.ok("品牌: " + brand + ", 颜色: " + color + ", 型号: " + model);
    }

    // 请求体处理
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserRequest request,
            HttpServletRequest httpRequest) {
        
        // 获取请求信息
        String userAgent = httpRequest.getHeader("User-Agent");
        String clientIP = getClientIP(httpRequest);
        
        // 处理业务逻辑
        UserDTO user = userService.createUser(request, userAgent, clientIP);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // 表单数据处理
    @PostMapping(value = "/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> handleFormData(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "age", required = false) Integer age,
            @RequestParam("interests") List<String> interests) {
        
        return ResponseEntity.ok("表单处理成功");
    }

    // 文件上传处理
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResult> handleFileUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", defaultValue = "general") String category) {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        // 文件验证
        validateFile(file);
        
        // 保存文件
        String filename = fileService.saveFile(file, category);
        
        UploadResult result = new UploadResult(filename, file.getSize(), description);
        return ResponseEntity.ok(result);
    }

    private void validateFile(MultipartFile file) {
        // 文件大小验证
        if (file.getSize() > 10 * 1024 * 1024) { // 10MB
            throw new IllegalArgumentException("文件大小不能超过10MB");
        }

        // 文件类型验证
        String contentType = file.getContentType();
        if (!isAllowedFileType(contentType)) {
            throw new IllegalArgumentException("不支持的文件类型: " + contentType);
        }
    }

    private boolean isAllowedFileType(String contentType) {
        return contentType != null && (
            contentType.startsWith("image/") ||
            contentType.equals("application/pdf") ||
            contentType.equals("text/plain")
        );
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
```

## 🔧 视图解析和模板引擎

### 1. 视图解析器配置

```java
@Configuration
@EnableWebMvc
public class ViewResolverConfig implements WebMvcConfigurer {

    // Thymeleaf 视图解析器
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver());
        templateEngine.setEnableSpringELCompiler(true);
        
        // 添加方言
        templateEngine.addDialect(new SpringSecurityDialect());
        templateEngine.addDialect(new Java8TimeDialect());
        
        return templateEngine;
    }

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setPrefix("classpath:/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(true);
        templateResolver.setCharacterEncoding("UTF-8");
        return templateResolver;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
        viewResolver.setTemplateEngine(templateEngine());
        viewResolver.setCharacterEncoding("UTF-8");
        viewResolver.setContentType("text/html; charset=UTF-8");
        viewResolver.setCache(true);
        return viewResolver;
    }

    // JSP 视图解析器（如果需要）
    @Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");
        viewResolver.setContentType("text/html; charset=UTF-8");
        viewResolver.setOrder(2); // 设置优先级，数字越小优先级越高
        return viewResolver;
    }

    // JSON 视图解析器
    @Bean
    public MappingJackson2JsonView jsonView() {
        MappingJackson2JsonView jsonView = new MappingJackson2JsonView();
        jsonView.setContentType("application/json; charset=UTF-8");
        jsonView.setPrettyPrint(true);
        return jsonView;
    }

    // 视图解析器链配置
    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        // 1. Thymeleaf 解析器（优先级最高）
        registry.viewResolver(thymeleafViewResolver());
        
        // 2. JSP 解析器
        registry.jsp("/WEB-INF/views/", ".jsp");
        
        // 3. 内容协商视图解析器
        registry.enableContentNegotiation(new MappingJackson2JsonView());
    }
}
```

### 2. 模型数据处理

```java
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public ProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // 全局模型属性
    @ModelAttribute("categories")
    public List<Category> getAllCategories() {
        return categoryService.findAll();
    }

    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpServletRequest request) {
        // 从会话或安全上下文获取当前用户
        return (User) request.getSession().getAttribute("currentUser");
    }

    // 产品列表页面
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 构建查询条件
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
            .category(category)
            .search(search)
            .page(page)
            .size(12)
            .build();

        // 查询产品
        Page<Product> products = productService.searchProducts(criteria);
        
        // 添加模型数据
        model.addAttribute("products", products);
        model.addAttribute("searchCriteria", criteria);
        model.addAttribute("pagination", createPaginationInfo(products));
        
        // 添加面包屑导航
        model.addAttribute("breadcrumbs", createBreadcrumbs(category));
        
        return "products/list";
    }

    // 产品详情页面
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        
        // 相关产品推荐
        List<Product> relatedProducts = productService.findRelatedProducts(product, 4);
        
        // 用户评价
        List<Review> reviews = reviewService.findByProduct(product, PageRequest.of(0, 5));
        
        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", reviewService.getAverageRating(product));
        
        return "products/detail";
    }

    // 创建产品表单
    @GetMapping("/create")
    public String createProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formAction", "create");
        return "products/form";
    }

    // 编辑产品表单
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("formAction", "edit");
        return "products/form";
    }

    // 处理表单提交
    @PostMapping
    public String saveProduct(@Valid @ModelAttribute Product product,
                            BindingResult bindingResult,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        
        // 验证失败
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", product.getId() == null ? "create" : "edit");
            return "products/form";
        }

        try {
            Product savedProduct = productService.save(product);
            redirectAttributes.addFlashAttribute("successMessage", "产品保存成功");
            return "redirect:/products/" + savedProduct.getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "保存失败: " + e.getMessage());
            model.addAttribute("formAction", product.getId() == null ? "create" : "edit");
            return "products/form";
        }
    }

    // AJAX 数据接口
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<ProductSuggestion>> searchSuggestions(
            @RequestParam String query) {
        
        List<ProductSuggestion> suggestions = productService.searchSuggestions(query);
        return ResponseEntity.ok(suggestions);
    }

    // 文件上传处理
    @PostMapping("/{id}/images")
    public String uploadProductImage(@PathVariable Long id,
                                   @RequestParam("image") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        
        try {
            String imageUrl = productService.uploadImage(id, file);
            redirectAttributes.addFlashAttribute("successMessage", "图片上传成功");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "图片上传失败: " + e.getMessage());
        }
        
        return "redirect:/products/" + id;
    }

    // 辅助方法
    private PaginationInfo createPaginationInfo(Page<Product> products) {
        return PaginationInfo.builder()
            .currentPage(products.getNumber())
            .totalPages(products.getTotalPages())
            .totalElements(products.getTotalElements())
            .hasNext(products.hasNext())
            .hasPrevious(products.hasPrevious())
            .build();
    }

    private List<Breadcrumb> createBreadcrumbs(String category) {
        List<Breadcrumb> breadcrumbs = new ArrayList<>();
        breadcrumbs.add(new Breadcrumb("首页", "/"));
        breadcrumbs.add(new Breadcrumb("产品", "/products"));
        
        if (category != null) {
            breadcrumbs.add(new Breadcrumb(category, "/products?category=" + category));
        }
        
        return breadcrumbs;
    }
}
```

### 3. 内容协商和多格式响应

```java
@RestController
@RequestMapping("/api/data")
public class ContentNegotiationController {

    private final DataService dataService;

    public ContentNegotiationController(DataService dataService) {
        this.dataService = dataService;
    }

    // 支持多种响应格式
    @GetMapping(value = "/users", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE,
        "text/csv"
    })
    public ResponseEntity<?> getUsers(
            @RequestHeader(value = "Accept", defaultValue = MediaType.APPLICATION_JSON_VALUE) String acceptHeader) {
        
        List<User> users = dataService.getAllUsers();
        
        // 根据 Accept 头确定响应格式
        if (acceptHeader.contains(MediaType.APPLICATION_XML_VALUE)) {
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(users);
        } else if (acceptHeader.contains("text/csv")) {
            String csvData = convertToCsv(users);
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header("Content-Disposition", "attachment; filename=users.csv")
                .body(csvData);
        } else {
            // 默认返回 JSON
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(users);
        }
    }

    // 下载文件
    @GetMapping("/export/users")
    public ResponseEntity<Resource> exportUsers(@RequestParam String format) throws IOException {
        
        Resource resource;
        String contentType;
        String filename;
        
        switch (format.toLowerCase()) {
            case "excel":
                resource = dataService.exportUsersToExcel();
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                filename = "users.xlsx";
                break;
            case "pdf":
                resource = dataService.exportUsersToPdf();
                contentType = "application/pdf";
                filename = "users.pdf";
                break;
            case "csv":
            default:
                resource = dataService.exportUsersToCsv();
                contentType = "text/csv";
                filename = "users.csv";
                break;
        }
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(resource);
    }

    // 图片处理
    @GetMapping("/images/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id,
                                         @RequestParam(defaultValue = "original") String size) {
        
        ImageData imageData = dataService.getImage(id, size);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(imageData.getContentType()))
            .contentLength(imageData.getSize())
            .header("Cache-Control", "max-age=3600") // 缓存1小时
            .body(imageData.getData());
    }

    private String convertToCsv(List<User> users) {
        StringBuilder csv = new StringBuilder();
        csv.append("ID,姓名,邮箱,年龄\n");
        
        for (User user : users) {
            csv.append(user.getId()).append(",")
               .append(user.getName()).append(",")
               .append(user.getEmail()).append(",")
               .append(user.getAge()).append("\n");
        }
        
        return csv.toString();
    }
}
```

## 🛡️ 拦截器和过滤器

### 1. 拦截器实现

```java
// 认证拦截器
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    private final AuthenticationService authService;
    private final Set<String> excludePatterns;

    public AuthenticationInterceptor(AuthenticationService authService) {
        this.authService = authService;
        this.excludePatterns = Set.of("/login", "/register", "/api/public/**", "/css/**", "/js/**");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        
        // 检查是否为排除路径
        if (isExcludedPath(requestURI)) {
            return true;
        }

        // 检查用户认证
        String token = extractToken(request);
        if (token == null || !authService.validateToken(token)) {
            handleUnauthorized(request, response);
            return false;
        }

        // 设置用户上下文
        User user = authService.getUserFromToken(token);
        UserContext.setCurrentUser(user);
        
        logger.debug("用户 {} 访问: {}", user.getUsername(), requestURI);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 在控制器执行后，视图渲染前执行
        if (modelAndView != null) {
            // 添加全局模型数据
            User currentUser = UserContext.getCurrentUser();
            if (currentUser != null) {
                modelAndView.addObject("currentUser", currentUser);
                modelAndView.addObject("userRole", currentUser.getRole());
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理用户上下文
        UserContext.clear();
        
        // 记录请求完成日志
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        logger.info("请求完成: {} - {}ms", request.getRequestURI(), duration);
    }

    private boolean isExcludedPath(String requestURI) {
        return excludePatterns.stream()
            .anyMatch(pattern -> requestURI.matches(pattern.replace("**", ".*")));
    }

    private String extractToken(HttpServletRequest request) {
        // 从 Header 获取 token
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // 从 Cookie 获取 token
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }

    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (isAjaxRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"error\":\"未授权访问\",\"code\":401}");
        } else {
            response.sendRedirect("/login?redirect=" + URLEncoder.encode(request.getRequestURI(), StandardCharsets.UTF_8));
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With")) ||
               request.getContentType() != null && request.getContentType().contains("application/json");
    }
}

// 性能监控拦截器
@Component
public class PerformanceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceInterceptor.class);
    private final MeterRegistry meterRegistry;

    public PerformanceInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute("startTime", System.currentTimeMillis());
        
        // 记录请求计数
        Timer.Sample sample = Timer.start(meterRegistry);
        request.setAttribute("timerSample", sample);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long duration = System.currentTimeMillis() - startTime;
        
        // 停止计时器
        Timer.Sample sample = (Timer.Sample) request.getAttribute("timerSample");
        sample.stop(Timer.builder("http.request.duration")
            .tag("method", request.getMethod())
            .tag("status", String.valueOf(response.getStatus()))
            .tag("uri", request.getRequestURI())
            .register(meterRegistry));

        // 记录慢请求
        if (duration > 1000) {
            logger.warn("慢请求检测: {} {} - {}ms", 
                request.getMethod(), request.getRequestURI(), duration);
        }

        // 记录错误请求
        if (ex != null) {
            logger.error("请求异常: {} {} - {}", 
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        }
    }
}

// 拦截器配置
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;
    private final PerformanceInterceptor performanceInterceptor;

    public InterceptorConfig(AuthenticationInterceptor authenticationInterceptor,
                           PerformanceInterceptor performanceInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.performanceInterceptor = performanceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 性能监控拦截器（所有请求）
        registry.addInterceptor(performanceInterceptor)
            .addPathPatterns("/**");

        // 认证拦截器（排除静态资源和公开接口）
        registry.addInterceptor(authenticationInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                "/css/**", "/js/**", "/images/**", "/favicon.ico",
                "/login", "/register", "/api/public/**"
            );
    }
}
```

### 2. 过滤器实现

```java
// 请求日志过滤器
@Component
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // 创建可重复读取的请求包装器
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(httpResponse);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 执行请求
            chain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequest(wrappedRequest, wrappedResponse, duration);
            
            // 复制响应内容
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request, 
                          ContentCachingResponseWrapper response, 
                          long duration) {
        
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        int status = response.getStatus();
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);
        
        StringBuilder logMessage = new StringBuilder()
            .append("HTTP Request: ")
            .append(method).append(" ")
            .append(uri);
        
        if (queryString != null) {
            logMessage.append("?").append(queryString);
        }
        
        logMessage.append(" - Status: ").append(status)
                  .append(", Duration: ").append(duration).append("ms")
                  .append(", Client: ").append(clientIP);
        
        if (userAgent != null) {
            logMessage.append(", User-Agent: ").append(userAgent);
        }
        
        // 记录请求体（仅POST/PUT/PATCH）
        if (shouldLogRequestBody(method)) {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0 && content.length < 1024) { // 限制大小
                String body = new String(content, StandardCharsets.UTF_8);
                logMessage.append(", Body: ").append(body);
            }
        }
        
        logger.info(logMessage.toString());
    }

    private boolean shouldLogRequestBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}

// CORS 过滤器
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        
        String origin = request.getHeader("Origin");
        
        // 设置CORS头
        response.setHeader("Access-Control-Allow-Origin", getAllowedOrigin(origin));
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", 
            "Authorization, Content-Type, X-Requested-With, Accept, Origin, User-Agent, Cache-Control");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Max-Age", "3600");
        
        // 处理预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(req, res);
    }

    private String getAllowedOrigin(String origin) {
        // 生产环境应该配置具体的允许域名
        List<String> allowedOrigins = List.of(
            "http://localhost:3000",
            "http://localhost:8080",
            "https://yourdomain.com"
        );
        
        if (origin != null && allowedOrigins.contains(origin)) {
            return origin;
        }
        
        return "*"; // 开发环境可以使用 *，生产环境建议具体指定
    }
}
```

## 📝 小结

Spring MVC 提供了完整的 Web 应用开发解决方案：

### 核心特性总结

- **DispatcherServlet** - 前端控制器，统一处理所有 Web 请求
- **控制器模式** - 支持传统 MVC 和 RESTful API 两种开发模式
- **视图解析** - 灵活的视图解析机制，支持多种模板引擎
- **请求处理** - 强大的参数绑定和数据转换能力
- **拦截器** - AOP 式的横切关注点处理

### 架构优势

| 特性 | 优势 | 应用场景 |
|------|------|----------|
| **组件化设计** | 高度可扩展和定制 | 复杂业务应用 |
| **注解驱动** | 简化开发配置 | 快速开发 |
| **视图无关** | 支持多种视图技术 | 多端应用 |
| **RESTful 支持** | 现代 API 设计 | 前后端分离 |

### 最佳实践要点

1. **控制器设计** - 保持控制器轻量，将业务逻辑放在服务层
2. **参数验证** - 使用 Bean Validation 进行输入验证
3. **异常处理** - 统一异常处理机制，提供友好错误信息
4. **性能优化** - 合理使用缓存和拦截器
5. **安全考虑** - 实施适当的认证和授权机制

Spring MVC 的灵活性和强大功能使其成为 Java Web 开发的首选框架，结合 Spring Boot 的自动配置，能够大大提高开发效率和应用质量。
