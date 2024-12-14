/** Turns out we do not need to track priority. It is 'only' a calculated value
  that we send when updating 'NEW" requests.
*/
alter table dispatch_request drop column priority;
