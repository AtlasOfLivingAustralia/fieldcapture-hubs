<div class="clearfix" data-bind="visible:transients.mobileApps().length > 0" style="margin:6px 0">
    Mobile Apps: <span data-bind="foreach:transients.mobileApps">
    <a data-bind="attr:{href:link.url}"><img class="logo-small" data-bind="attr:{src:logo('${imageUrl}')}"/></a>
</span>
</div>
<div class="clearfix" data-bind="visible:transients.socialMedia().length > 0" style="margin:6px 0">
    Social Media: <span data-bind="foreach:transients.socialMedia">
    <a data-bind="attr:{href:link.url}"><img class="logo-small" data-bind="attr:{src:logo('${imageUrl}')}"/></a>
</span>
</div>
