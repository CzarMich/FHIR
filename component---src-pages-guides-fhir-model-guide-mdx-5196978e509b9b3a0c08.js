(window.webpackJsonp=window.webpackJsonp||[]).push([[11],{375:function(e,t,a){"use strict";a.r(t),a.d(t,"_frontmatter",function(){return o}),a.d(t,"default",function(){return c});a(10),a(5),a(6),a(3),a(8),a(2),a(1);var n=a(104),r=a(393);function i(){return(i=Object.assign||function(e){for(var t=1;t<arguments.length;t++){var a=arguments[t];for(var n in a)Object.prototype.hasOwnProperty.call(a,n)&&(e[n]=a[n])}return e}).apply(this,arguments)}var o={},s={_frontmatter:o},l=r.a;function c(e){var t=e.components,a=function(e,t){if(null==e)return{};var a,n,r={},i=Object.keys(e);for(n=0;n<i.length;n++)a=i[n],t.indexOf(a)>=0||(r[a]=e[a]);return r}(e,["components"]);return Object(n.b)(l,i({},s,a,{components:t,mdxType:"MDXLayout"}),Object(n.b)("h2",{id:"overview"},"Overview"),Object(n.b)("p",null,"The FHIR model component provides Java APIs for parsing, building, generating and validating FHIR resources. Java model classes that represent FHIR resources and data types are generated directly from the structure definitions distributed with the spec. All model objects are thread-safe and immutable. Each model class implements the Java builder pattern (Effective Java, Joshua Bloch) and the visitor pattern (GoF). The classes also implement Java equals, hashCode and toString methods. All date/time processing is done using the Java 8 time library."),Object(n.b)("p",null,"Many of the data type classes include additional factory methods to facilitate object construction for common use cases. The model includes generated Javadoc comments complete with excerpts taken directly from the specification. Model classes also include Java annotations for constraints (",Object(n.b)("inlineCode",{parentName:"p"},"@Constraint"),"), required elements (",Object(n.b)("inlineCode",{parentName:"p"},"@Required"),"), choice element types (",Object(n.b)("inlineCode",{parentName:"p"},"@Choice"),") and value set bindings (",Object(n.b)("inlineCode",{parentName:"p"},"@Binding"),"). Value set bindings are implemented using Code subclasses with constant fields and nested enumerations. Backbone elements are implemented as Java nested classes to keep them organized."),Object(n.b)("p",null,"All schema-level (structure, cardinality, value domain) and global (empty resource, empty element) constraint validation is happens during object construction. This means that it is virtually impossible to build a schema invalid FHIR resource using the APIs. Additional constraint validation (invariants, profile, terminology) is performed using the FHIRValidator class. FHIRParser and FHIRGenerator classes are used to parse and generate both JSON and XML formats. FHIRPathEvaluator is a FHIRPath evaluation engine built on an ANTLR4 generated parser. It implements are large portion of the FHIRPath specification and is used for validation and search parameter value extraction."),Object(n.b)("h2",{id:"building-a-resource-using-the-fhir-model-api"},"Building a Resource using the FHIR Model API"),Object(n.b)("p",null,"The FHIR model API implements the Builder pattern for constructing Resource instances."),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),'Observation bodyWeight = Observation.builder()\n    .meta(Meta.builder()\n        .profile(Canonical.of("http://hl7.org/fhir/StructureDefinition/bodyweight"))\n        .build())\n    .status(ObservationStatus.FINAL)\n    .effective(DateTime.builder()\n        .value("2019-01-01")\n        .build())\n    .category(CodeableConcept.builder()\n        .coding(Coding.builder()\n            .system(Uri.of("http://terminology.hl7.org/CodeSystem/observation-category"))\n            .code(Code.of("vital-signs"))\n            .build())\n        .build())\n    .code(CodeableConcept.builder()\n        .coding(Coding.builder()\n            .system(Uri.of("http://loinc.org"))\n            .code(Code.of("29463-7"))\n            .build())\n        .build())\n    .value(Quantity.builder()\n        .value(Decimal.of(200))\n        .system(Uri.of("http://unitsofmeasure.org"))\n        .code(Code.of("[lb_av]"))\n        .unit(string("lbs"))\n        .build())\n    .build();\n')),Object(n.b)("p",null,"In the example above, a number of different builder classes are used:"),Object(n.b)("ul",null,Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"Observation.Builder")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"DateTime.Builder")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"CodeableConcept.Builder")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"Quantity.Builder"))),Object(n.b)("p",null,"Every type in the model that represents a FHIR resource or element has a corresponding nested, static Builder class used for constructing thread-safe, immutable instances."),Object(n.b)("p",null,"Several static factory / utility methods are also used:"),Object(n.b)("ul",null,Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"Canonical.of(...)")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"Uri.of(...)")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"Code.of(...)")),Object(n.b)("li",{parentName:"ul"},Object(n.b)("inlineCode",{parentName:"li"},"String.string(...)")," (via static import)")),Object(n.b)("p",null,"Many of the primitive data types contain this type of “helper” method."),Object(n.b)("p",null,"Fields from an immutable model object may be copied back into a builder object using the ",Object(n.b)("inlineCode",{parentName:"p"},"toBuilder()")," method:"),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),"bodyWeight = bodyWeight.toBuilder()\n    .value(bodyWeight.getValue().as(Quantity.class).toBuilder()\n        .value(Decimal.of(210))\n        .build())\n    .build();\n")),Object(n.b)("h2",{id:"parsing-a-resource-from-an-inputstream-or-reader"},"Parsing a Resource from an InputStream or Reader"),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),'// Parse from InputStream\nInputStream in = getInputStream("JSON/bodyweight.json");\nObservation observation = FHIRParser.parser(Format.JSON).parse(in);\n\n// Parse from Reader\nReader reader = getReader("JSON/bodyweight.json");\nObservation observation = FHIRParser.parser(Format.JSON).parse(reader);\n')),Object(n.b)("h2",{id:"generating-json-and-xml-formats-from-a-resource-instance"},"Generating JSON and XML formats from a Resource instance"),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),"// Generate JSON format\nFHIRGenerator.generator(Format.JSON).generate(bodyWeight, System.out);\n\n// Generate XML format\nFHIRGenerator.generator(Format.XML).generate(bodyWeight, System.out);\n")),Object(n.b)("p",null,"The ",Object(n.b)("inlineCode",{parentName:"p"},"FHIRGenerator")," interface has a separate factory method that takes ",Object(n.b)("inlineCode",{parentName:"p"},"boolean prettyPrinting")," as a parameter:"),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),"// Generate JSON format (with pretty printing)\nFHIRGenerator.generator(Format.JSON, true).generate(bodyWeight, System.out);\n")),Object(n.b)("h2",{id:"validating-a-resource-instance"},"Validating a Resource instance"),Object(n.b)("p",null,"Schema-level validation occurs during object construction. This includes validation of cardinality constraints and value domains. Additional validation of constraints specified in the model is performed using the ",Object(n.b)("inlineCode",{parentName:"p"},"FHIRValidator")," class."),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),"Observation observation = getObservation();\n\nList<Issue> issues = FHIRValidator.validator().validate(observation);\n\nfor (Issue issue : issues) {\n    if (IssueSeverity.ERROR.equals(issue.getSeverity())) {\n        // handle error\n    }\n}\n")),Object(n.b)("h2",{id:"evaluating-fhirpath-expressions-on-a-resource-instance"},"Evaluating FHIRPath expressions on a Resource instance"),Object(n.b)("pre",null,Object(n.b)("code",i({parentName:"pre"},{}),'EvaluationContext evaluationContext = new EvaluationContext(bodyWeight);\nCollection<FHIRPathNode> result = FHIRPathEvaluator.evaluator().evaluate(evaluationContext, "Observation.value.as(Quantity).value >= 200");\nassert(FHIRPathUtil.isTrue(result));\n')),Object(n.b)("p",null,"The ",Object(n.b)("inlineCode",{parentName:"p"},"EvaluationContext")," class builds a ",Object(n.b)("inlineCode",{parentName:"p"},"FHIRPathTree")," from a FHIR resource or element. A ",Object(n.b)("inlineCode",{parentName:"p"},"FHIRPathTree")," is a tree of labeled nodes that wrap FHIR elements and are used by the FHIRPath evaluation engine (",Object(n.b)("inlineCode",{parentName:"p"},"FHIRPathEvaluator"),")."))}c.isMDXComponent=!0},391:function(e){e.exports={data:{site:{pathPrefix:"/FHIR"}}}},392:function(e){e.exports={data:{site:{siteMetadata:{repository:{baseUrl:"https://github.com/IBM/FHIR",subDirectory:"/docs"}}}}}},393:function(e,t,a){"use strict";a(34),a(23);var n=a(2),r=a(391),i=a(1),o=a.n(i),s=a(192),l=a(90),c=a.n(l),b=a(91),d=a(123),u=a(4),p=a.n(u),m=a(366),h=function(e){var t,a=e.children,r=e.title,i=e.tabs,o=void 0===i?[]:i,s=e.shouldHideHeader;return Object(n.b)("div",{className:p()((t={},t[m.pageHeader]=m.pageHeader,t[m.pageHeaderSticky]=o.length,t[m.pageHeaderShifted]=s,t))},Object(n.b)("div",{className:"bx--grid"},Object(n.b)("div",{className:"bx--row"},Object(n.b)("div",{className:"bx--col-lg-12"},Object(n.b)("h1",{id:"page-title",className:m.text},r)))),a)},O=a(392),v=a(367),g=function(e){var t=e.relativePagePath,a=e.repository,r=O.data.site.siteMetadata.repository,i=a||r,o=i.baseUrl,s=o+"/tree/master"+i.subDirectory+"/src/pages"+t;return o?Object(n.b)("div",{className:"bx--row "+v.row},Object(n.b)("div",{className:"bx--col"},Object(n.b)("a",{className:v.link,href:s},"Edit this page on GitHub"))):null},f=a(193),j=a(28),y=a(368);var N=function(e){var t,a;function r(){return e.apply(this,arguments)||this}return a=e,(t=r).prototype=Object.create(a.prototype),t.prototype.constructor=t,t.__proto__=a,r.prototype.render=function(){var e=this.props,t=e.tabs,a=e.slug,r=a.split("/").filter(Boolean).slice(-1)[0],i=t.map(function(e){var t,i=c()(e,{lower:!0}),o=i===r,s=a.replace(r,i);return Object(n.b)("li",{key:e,className:p()((t={},t[y.selectedItem]=o,t),y.listItem)},Object(n.b)(j.Link,{className:y.link,to:""+s},e))});return Object(n.b)("div",{className:y.tabsContainer},Object(n.b)("div",{className:"bx--grid"},Object(n.b)("div",{className:"bx--row"},Object(n.b)("div",{className:"bx--col-lg-12 bx--col-no-gutter"},Object(n.b)("nav",null,Object(n.b)("ul",{className:y.list},i))))))},r}(o.a.Component),C=a(194);t.a=function(e){var t=e.pageContext,a=e.children,i=e.location,o=t.frontmatter,l=void 0===o?{}:o,u=t.relativePagePath,p=l.tabs,m=l.title,O=Object(b.c)(),v=!!p&&"down"===O,j=r.data.site.pathPrefix,y=j?i.pathname.replace(j,""):i.pathname,I=p?y.split("/").slice(-1)[0]||c()(p[0],{lower:!0}):"";return Object(n.b)(d.a,{shouldHideHeader:v,homepage:!1},Object(n.b)(h,{shouldHideHeader:v,title:m,label:"label",tabs:p},p&&Object(n.b)(N,{slug:y,tabs:p,currentTab:I})),Object(n.b)(C.a,{padded:!0},a,Object(n.b)(g,{relativePagePath:u})),Object(n.b)(f.a,{pageContext:t,location:i,slug:y,tabs:p,currentTab:I}),Object(n.b)(s.a,null))}}}]);
//# sourceMappingURL=component---src-pages-guides-fhir-model-guide-mdx-5196978e509b9b3a0c08.js.map