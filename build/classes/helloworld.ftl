=======================
===  Category List ====
=======================
<#list Categories as category>
    ${category_index + 1}. ${category.value!"NullName"} from ${category.name!"NullValue"} 
</#list>
