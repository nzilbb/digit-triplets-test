<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" 
%><%@ taglib prefix="hex" tagdir="/WEB-INF/tags" 
%>
<hex:blockmenu>
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/instances"
     text="Instances" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/fields"
     text="Form Fields" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=introduction"
     text="Text: Introduction" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=test"
     text="Text: Test - Speakers" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=testl"
     text="Text: Test - Left" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=testr"
     text="Text: Test - Right" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=result-2"
     text="Text: Incomplete" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=result-1"
     text="Text: Poor" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=result0"
     text="Text: Inconclusive" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=result1"
     text="Text: Normal" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultl-2"
     text="Text: Incomplete - Left" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultl-1"
     text="Text: Poor - Left" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultl0"
     text="Text: Inconclusive - Left" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultl1"
     text="Text: Normal - Left" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultr-2"
     text="Text: Incomplete - Right" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultr-1"
     text="Text: Poor - Right" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultr0"
     text="Text: Inconclusive - Right" />
  <hex:blockmenuitem 
     urlModule="${block.module}" urlPath="admin/text?file=resultr1"
     text="Text: Normal - Right" />
</hex:blockmenu>
