{
  "@type": "MessageCard",
  "@context": "https:\/\/schema.org\/extensions",
  "summary": "new content from Alert",
  "themeColor": "5A2A82",
  "title": "New content from ${providerCount} Provider<#if providerCount gt 1>s</#if>",
  "sections": [
<#list sections as section>
    {
      "startGroup": true,
      "title": "${section.provider?json_string} - ${section.topic?json_string}<#if section.subTopic??> - ${section.subTopic?json_string}</#if>",
      "text": "<#list section.components as component>* ${component.category?json_string} (${component.operation?json_string}): ${component.details?json_string}<#sep>${'\n'}</#sep></#list>"
    }<#sep>, </#sep>
</#list>
  ]
}
