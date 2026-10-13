import 'package:flutter/material.dart';

/// Toutes les icônes de l'app passent ici : style unique (contour arrondi) et remplaçable
/// en bloc par un jeu SVG sans toucher aux écrans. Ne jamais écrire `Icons.xxx` dans un écran.
class AppIcons {
  const AppIcons._();

  // Navigation (repos = contour, actif = plein)
  static const home = Icons.home_outlined;
  static const homeActive = Icons.home_rounded;
  static const passport = Icons.badge_outlined;
  static const passportActive = Icons.badge_rounded;
  static const talents = Icons.travel_explore_outlined;
  static const talentsActive = Icons.travel_explore_rounded;
  static const notifications = Icons.notifications_none_rounded;
  static const notificationsActive = Icons.notifications_rounded;
  static const more = Icons.grid_view_outlined;
  static const moreActive = Icons.grid_view_rounded;

  // Fonctionnalités
  static const competences = Icons.workspace_premium_outlined;
  static const experiences = Icons.work_outline_rounded;
  static const portfolio = Icons.photo_library_outlined;
  static const preuves = Icons.verified_outlined;
  static const validations = Icons.fact_check_outlined;
  static const tests = Icons.quiz_outlined;
  static const relations = Icons.handshake_outlined;
  static const opportunites = Icons.campaign_outlined;

  // Média (sélection de fichiers)
  static const camera = Icons.photo_camera_outlined;
  static const gallery = Icons.photo_outlined;
  static const video = Icons.videocam_outlined;
  static const document = Icons.description_outlined;
  static const upload = Icons.cloud_upload_outlined;
  static const close = Icons.close_rounded;

  // Actions et états
  static const add = Icons.add_rounded;
  static const back = Icons.arrow_back_rounded;
  static const chevron = Icons.chevron_right_rounded;
  static const copy = Icons.copy_rounded;
  static const visible = Icons.visibility_outlined;
  static const hidden = Icons.visibility_off_outlined;
  static const person = Icons.person_outline_rounded;
  static const empty = Icons.inbox_outlined;
  static const error = Icons.error_outline_rounded;
  static const offline = Icons.wifi_off_rounded;
  static const forbidden = Icons.lock_outline_rounded;
  static const notFound = Icons.search_off_rounded;
  static const construction = Icons.construction_outlined;

  static const selected = Icons.check_circle_rounded;
  static const unselected = Icons.radio_button_unchecked_rounded;
  static const edit = Icons.edit_outlined;
  static const expand = Icons.expand_more_rounded;
  static const location = Icons.place_outlined;
  static const history = Icons.history_rounded;

  static const calendar = Icons.calendar_today_outlined;
  static const delete = Icons.delete_outline_rounded;
  static const markAllRead = Icons.done_all_rounded;
  static const unreadDot = Icons.circle;
  static const refresh = Icons.refresh_rounded;

  // Icônes ajoutées pour les parcours mobiles.
  static const search = Icons.search_rounded;
  static const filter = Icons.filter_alt_outlined;
  static const lock = Icons.lock_outline_rounded;
  static const phone = Icons.phone_outlined;
  static const email = Icons.email_outlined;
  static const clock = Icons.schedule_outlined;
  static const check = Icons.check_rounded;
  static const arrowRight = Icons.arrow_forward_rounded;
  static const tune = Icons.tune_rounded;
  static const logout = Icons.logout_rounded;
  static const dataSaver = Icons.data_saver_on_outlined;
  static const opportunityCategory = Icons.category_outlined;
  static const training = Icons.school_outlined;
  static const scholarship = Icons.account_balance_outlined;
  static const program = Icons.auto_awesome_outlined;
  static const callForApplications = Icons.assignment_outlined;
  static const competition = Icons.emoji_events_outlined;
  static const support = Icons.support_agent_outlined;
}
