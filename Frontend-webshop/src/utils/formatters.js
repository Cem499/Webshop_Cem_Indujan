// Formatiert einen ISO-Datumsstring in die Schweizer Darstellung (dd.mm.yyyy, hh:mm:ss)
export const formatDate = (dateString) => new Date(dateString).toLocaleString('de-CH');

// Gibt inline CSS-Stile für den Bestellstatus-Badge zurück.
// OFFEN = gelb, BEZAHLT = grün, alles andere (STORNIERT) = rot
export const statusBadgeStyle = (status) => ({
    padding: '0.25rem 0.5rem',
    borderRadius: '4px',
    fontSize: '0.8rem',
    background: status === 'OFFEN' ? '#fff3cd' : status === 'BEZAHLT' ? '#d4edda' : '#f8d7da',
    color: status === 'OFFEN' ? '#856404' : status === 'BEZAHLT' ? '#155724' : '#721c24'
});
