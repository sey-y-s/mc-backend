class UserAccount {
  const UserAccount({required this.id, required this.telephone, this.email});
  final String id;
  final String telephone;
  final String? email;

  factory UserAccount.fromJson(Map<String, dynamic> json) => UserAccount(
        id: (json['id'] ?? '').toString(),
        telephone: (json['telephone'] ?? '').toString(),
        email: json['email']?.toString(),
      );
}

class AccountChanges {
  const AccountChanges({required this.telephone, this.email});
  final String telephone;
  final String? email;
}

class AppPreferences {
  const AppPreferences({
    this.lowDataMode = false,
    this.notificationsEnabled = true,
  });
  final bool lowDataMode;
  final bool notificationsEnabled;

  AppPreferences copyWith({bool? lowDataMode, bool? notificationsEnabled}) =>
      AppPreferences(
        lowDataMode: lowDataMode ?? this.lowDataMode,
        notificationsEnabled: notificationsEnabled ?? this.notificationsEnabled,
      );
}
