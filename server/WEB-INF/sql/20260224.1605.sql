/* Update setting description */
UPDATE attribute SET description = 'The number of seconds to wait before timing-out a trial and allowing the next trial to start. Enter 0 for no time-out.'
WHERE attribute = 'timeoutseconds';
