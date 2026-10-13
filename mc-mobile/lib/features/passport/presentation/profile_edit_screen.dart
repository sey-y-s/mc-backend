import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mlc_mobile/app/theme/app_colors.dart';
import 'package:mlc_mobile/core/errors/app_failure.dart';
import 'package:mlc_mobile/core/media/picked_media.dart';
import 'package:mlc_mobile/core/utils/snackbars.dart';
import 'package:mlc_mobile/core/utils/validators.dart';
import 'package:mlc_mobile/core/widgets/app_text_field.dart';
import 'package:mlc_mobile/core/widgets/file_picker_field.dart';
import 'package:mlc_mobile/core/widgets/filter_chips.dart';
import 'package:mlc_mobile/core/widgets/form_scaffold.dart';
import 'package:mlc_mobile/core/widgets/state_views.dart';
import 'package:mlc_mobile/features/passport/domain/passport_models.dart';
import 'package:mlc_mobile/features/passport/presentation/passport_providers.dart';

/// Route : /passeport/profil
/// TODO(PISTE A): suppression de la photo (endpoint à définir) ; pour l'instant « Retirer » ne supprime que l'aperçu.
class ProfileEditScreen extends ConsumerWidget {
  const ProfileEditScreen({super.key});

  Widget _shell(Widget body) =>
      Scaffold(appBar: AppBar(title: const Text('Mon profil')), body: body);

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    // skipLoadingOnReload : le formulaire garde sa saisie quand le passeport est rechargé (ex. après l'envoi de la photo).
    return ref.watch(passportProvider).when(
          skipLoadingOnReload: true,
          loading: () => _shell(const LoadingView()),
          error: (e, _) => _shell(ErrorView(
              error: e, onRetry: () => ref.invalidate(passportProvider))),
          data: (c) => _ProfileForm(citoyen: c),
        );
  }
}

class _ProfileForm extends ConsumerStatefulWidget {
  const _ProfileForm({required this.citoyen});
  final Citoyen citoyen;

  @override
  ConsumerState<_ProfileForm> createState() => _ProfileFormState();
}

class _ProfileFormState extends ConsumerState<_ProfileForm> {
  final _formKey = GlobalKey<FormState>();
  late final _nom = TextEditingController(text: widget.citoyen.nom);
  late final _prenom = TextEditingController(text: widget.citoyen.prenom);
  late Sexe? _sexe = widget.citoyen.sexe;
  PickedMedia? _photo;

  @override
  void dispose() {
    _nom.dispose();
    _prenom.dispose();
    super.dispose();
  }

  Future<void> _onPhoto(PickedMedia? media) async {
    setState(() => _photo = media);
    if (media == null) return;
    final ok =
        await ref.read(passportActionsProvider.notifier).updatePhoto(media);
    if (!ok && mounted) {
      final error = ref.read(passportActionsProvider).error;
      showError(
          context, error == null ? 'Envoi impossible.' : failureMessage(error));
      setState(() => _photo = null);
    }
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    final ok = await ref.read(passportActionsProvider.notifier).updateProfile(
          nom: _nom.text.trim(),
          prenom: _prenom.text.trim(),
          sexe: _sexe,
        );
    if (!ok || !mounted) return;
    showSuccess(context, 'Profil enregistré');
    Navigator.of(context).maybePop();
  }

  @override
  Widget build(BuildContext context) {
    final actions = ref.watch(passportActionsProvider);
    final progress = ref.watch(photoProgressProvider);
    final busy = actions.isLoading;
    return FormScaffold(
      title: 'Mon profil',
      formKey: _formKey,
      submitLabel: 'Enregistrer',
      onSubmit: _submit,
      isLoading: busy,
      error: actions.hasError ? actions.error : null,
      children: [
        FilePickerField(
          label: widget.citoyen.photoUrl == null
              ? 'Ajouter une photo'
              : 'Changer ma photo',
          allowed: const {MediaKind.image},
          value: _photo,
          uploadProgress: _photo == null ? null : progress,
          onChanged: _onPhoto,
        ),
        AppTextField(
            label: 'Nom',
            controller: _nom,
            enabled: !busy,
            textInputAction: TextInputAction.next,
            validator: (v) => Validators.required(v, field: 'Le nom')),
        AppTextField(
            label: 'Prénom',
            controller: _prenom,
            enabled: !busy,
            textInputAction: TextInputAction.next,
            validator: (v) => Validators.required(v, field: 'Le prénom')),
        Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
          Text('Sexe',
              style: Theme.of(context)
                  .textTheme
                  .bodySmall
                  ?.copyWith(color: AppColors.muted)),
          const SizedBox(height: 8),
          FilterChips<Sexe>(
            options: Sexe.values,
            labelOf: (s) => s.label,
            selected: _sexe,
            allLabel: 'Non précisé',
            onChanged: (v) => setState(() => _sexe = v),
          ),
        ]),
      ],
    );
  }
}
