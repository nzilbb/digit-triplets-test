/* Settings */
INSERT INTO attribute (description,attribute, value, type) VALUES
('The highest a signal level can be (if they go beyond it, the test ends)',
 'signallevelmaximum','4', 'integer'),
('The lowest a signel level can be (if they go beyond it, it stays at this level)',
 'signallevelminimum','-22', 'integer'),
('Whether to stop the test when the signal level goes above the maximum (true) or to continue the test at the maximum signal level (false)'
 ,'stopwhensignaltoohigh','false', 'boolean'),
('The amount added to the signal level when they get a triplet right, or subtracted when they get a triplet wrong',
 'signallevelincrement','-2', 'integer'),
('The signal level of the first trial',
 'signallevelstart','2', 'integer'),
('The number of seconds to wait before timing-out a trial and allowing the next trial to start',
 'timeoutseconds','10', 'integer'),
('The number of trials to ignore before starting to count trials for the test result',
 'ignoretrials','7', 'integer'),
('Poor SNR threshold',
 'snrpoormin','-8.52', 'number'),
('Normal SNR threshold',
 'snrnormalmax','-10.32', 'number');
