

select s.name supplier_name, sc.name supplier_county, i.name item, n.name dest_name, cn.name dest_county
from site_supply ss
join site s on s.id = ss.site_id
join county sc on sc.id = s.county_id
join item i on i.id = ss.item_id

join site_need sn on ss.item_id = sn.item_id
join site n on n.id = sn.site_id
join county cn on cn.id = n.county_id
where s.id <> n.id
order by s.name, sc.name, cn.name, n.name, i.name;


-- list of counties with supplies
select distinct c.name
from site_supply ss
join site s on s.id = ss.site_id
join county c on c.id = s.county_id
order by c.name;


