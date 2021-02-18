/* Add text for DTT Help Panel */
UPDATE text SET display_order = display_order + 1 WHERE display_order >= 5;
INSERT INTO text (id, label, display_order, html) VALUES
('dtt-help', 'Test Help Panel', 5, '<p>You will hear three digits.</p> <p>Enter the digits you hear, then press ''Next'' to hear the next three digits.</p>')
