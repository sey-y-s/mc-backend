const _mois = ['janvier', 'février', 'mars', 'avril', 'mai', 'juin', 'juillet', 'août', 'septembre', 'octobre', 'novembre', 'décembre'];

/// « 12 janvier 2026 » (sans dépendre des données de locale d'intl).
String formatDateFr(DateTime d) => '${d.day == 1 ? '1er' : d.day} ${_mois[d.month - 1]} ${d.year}';

/// « mars 2023 » : précision suffisante pour une période d'expérience.
String formatMonthYearFr(DateTime d) => '${_mois[d.month - 1]} ${d.year}';

/// « 2026-03-07 » : format d'échange avec l'API pour une date sans heure.
String isoDate(DateTime d) =>
    '${d.year.toString().padLeft(4, '0')}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';