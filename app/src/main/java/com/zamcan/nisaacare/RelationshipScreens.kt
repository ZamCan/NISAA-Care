package com.zamcan.nisaacare

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import com.zamcan.nisaacare.domain.model.DomainResult
import com.zamcan.nisaacare.domain.model.PairingInvitation
import com.zamcan.nisaacare.domain.model.PermissionKey
import com.zamcan.nisaacare.domain.model.Relationship
import com.zamcan.nisaacare.domain.model.RelationshipPermissions
import com.zamcan.nisaacare.domain.model.RelationshipStatus
import com.zamcan.nisaacare.domain.model.SupportRequest
import com.zamcan.nisaacare.domain.model.UserRole
import com.zamcan.nisaacare.domain.relationship.PairingLinks
import com.zamcan.nisaacare.domain.relationship.PairingPayload
import com.zamcan.nisaacare.ui.PairingQrCode
import com.zamcan.nisaacare.ui.NisaaDesign
import java.util.UUID

internal fun MainActivity.buildMarriageScreen(): View {
    return standardScreen(getString(R.string.marriage_title)) { body ->
        addSpaced(
            body,
            NisaaDesign.heroCard(
                this,
                getString(R.string.marriage_title),
                getString(R.string.app_name),
                getString(R.string.marriage_intro),
                R.drawable.nisaa_hero_marriage
            ),
            14
        )
        cardTitle(body, getString(R.string.marriage_title), getString(R.string.marriage_intro))
        val relationships = profile?.id?.let { repository.getRelationships(it) }.orEmpty()
        if (relationships.isEmpty()) {
            addSpaced(body, NisaaDesign.statePanel(this, getString(R.string.no_relationship), getString(R.string.connection_body), R.drawable.ic_relationship), 0)
        } else {
            relationships.forEach { relationship ->
                addSpaced(body, relationshipCard(relationship), 12)
            }
        }
        addSpaced(body, pairingCard(), 18)
        if (profile?.role == UserRole.HUSBAND) {
            addSpaced(body, NisaaDesign.softCard(this, 18).apply {
                addView(NisaaDesign.body(this@buildMarriageScreen, getString(R.string.husband_permission_required)))
            }, 0)
        }
        addSpaced(body, NisaaDesign.outlineButton(this, getString(R.string.settings_privacy), R.drawable.ic_lock) { navigate(AppScreen.PRIVACY) }, 18)
    }
}

private fun MainActivity.relationshipCard(relationship: Relationship): LinearLayout {
    val card = NisaaDesign.card(this, 18)
    val header = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    header.addView(NisaaDesign.icon(this@relationshipCard, R.drawable.ic_relationship, R.color.nisaa_rose, 25))
    header.addView(NisaaDesign.space(this@relationshipCard, 10, 1))
    header.addView(NisaaDesign.text(this@relationshipCard, getString(R.string.relationship_status), 15f, R.color.nisaa_ink, true).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    header.addView(NisaaDesign.statusPill(this@relationshipCard, relationshipStatusLabel(relationship.status), relationship.status == RelationshipStatus.ACTIVE))
    card.addView(header)
    card.addView(NisaaDesign.space(this@relationshipCard, 0, 6))
    card.addView(NisaaDesign.body(this@relationshipCard, getString(R.string.permission_explicit)))
    if (relationship.status == RelationshipStatus.ACTIVE) {
        card.addView(NisaaDesign.space(this@relationshipCard, 0, 14))
        card.addView(NisaaDesign.eyebrow(this@relationshipCard, getString(R.string.sharing_permissions)))
        card.addView(NisaaDesign.space(this@relationshipCard, 0, 7))
        PermissionKey.entries.forEach { permission -> card.addView(permissionRow(relationship, permission)) }
        card.addView(NisaaDesign.space(this@relationshipCard, 0, 8))
        card.addView(NisaaDesign.secondaryButton(this@relationshipCard, getString(R.string.support_request), R.drawable.ic_share) {
            supportRequestDialog(relationship)
        })
        card.addView(NisaaDesign.space(this@relationshipCard, 0, 8))
        card.addView(NisaaDesign.outlineButton(this@relationshipCard, getString(R.string.revoke_relationship), R.drawable.ic_lock) {
            confirmRevoke(relationship)
        })
    } else {
        card.addView(NisaaDesign.space(this@relationshipCard, 0, 12))
        card.addView(NisaaDesign.body(this@relationshipCard, getString(R.string.pairing_not_configured)))
    }
    return card
}

private fun MainActivity.permissionRow(relationship: Relationship, permission: PermissionKey): View {
    val row = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = NisaaDesign.dp(this@permissionRow, 48)
    }
    row.addView(NisaaDesign.body(this, permissionLabel(permission)).apply {
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
    })
    val enabled = relationship.permissions.permissions[permission] == true
    val toggle = Switch(this).apply {
        isChecked = enabled
        isEnabled = profile?.role == UserRole.WOMAN && relationship.status == RelationshipStatus.ACTIVE
        contentDescription = permissionLabel(permission)
        setOnCheckedChangeListener { _, checked ->
            if (isEnabled) {
                val result = repository.updatePermission(relationship, permission, checked)
                if (result is DomainResult.Failure) showToast(result.error.message)
                render()
            }
        }
    }
    row.addView(toggle)
    return row
}

private fun MainActivity.relationshipStatusLabel(status: RelationshipStatus): String = when (status) {
    RelationshipStatus.ACTIVE -> getString(R.string.relationship_active)
    RelationshipStatus.PENDING -> getString(R.string.relationship_pending)
    RelationshipStatus.REVOKED -> getString(R.string.relationship_revoked)
    RelationshipStatus.EXPIRED -> getString(R.string.state_empty)
}

private fun MainActivity.pairingCard(): LinearLayout = NisaaDesign.card(this, 20).apply {
    addView(NisaaDesign.icon(this@pairingCard, R.drawable.ic_share, R.color.nisaa_rose, 28))
    addView(NisaaDesign.space(this@pairingCard, 0, 10))
    addView(NisaaDesign.sectionTitle(this@pairingCard, getString(R.string.connection_title)))
    addView(NisaaDesign.body(this@pairingCard, getString(R.string.connection_body)))
    val invitation = lastInvitation
    if (invitation != null && invitation.isUsable()) {
        val payload = PairingPayload.fromInvitation(invitation)
        addView(NisaaDesign.space(this@pairingCard, 0, 14))
        addView(NisaaDesign.statusPill(this@pairingCard, getString(R.string.invitation_created), true))
        addView(NisaaDesign.space(this@pairingCard, 0, 8))
        addView(NisaaDesign.text(this@pairingCard, invitation.token, 13f, R.color.nisaa_ink, true, Gravity.CENTER))
        addView(NisaaDesign.space(this@pairingCard, 0, 4))
        addView(NisaaDesign.body(this@pairingCard, getString(R.string.invitation_expires)).apply { gravity = Gravity.CENTER })
        addView(NisaaDesign.space(this@pairingCard, 0, 13))
        val qr = ImageView(this@pairingCard).apply {
            setImageBitmap(PairingQrCode.create(payload.toUri(), NisaaDesign.dp(this@pairingCard, 260)))
            adjustViewBounds = true
            contentDescription = getString(R.string.pairing_qr_title)
            setPadding(NisaaDesign.dp(this@pairingCard, 10), NisaaDesign.dp(this@pairingCard, 10), NisaaDesign.dp(this@pairingCard, 10), NisaaDesign.dp(this@pairingCard, 10))
        }
        addView(qr, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, NisaaDesign.dp(this@pairingCard, 280)))
        addView(NisaaDesign.body(this@pairingCard, getString(R.string.pairing_qr_body)).apply { gravity = Gravity.CENTER })
        addView(NisaaDesign.space(this@pairingCard, 0, 12))
        addView(NisaaDesign.primaryButton(this@pairingCard, getString(R.string.share_whatsapp), R.drawable.ic_share) { shareInvitation(invitation) })
        addView(NisaaDesign.space(this@pairingCard, 0, 8))
        addView(NisaaDesign.secondaryButton(this@pairingCard, getString(R.string.pairing_show_qr), R.drawable.ic_share) { showPairingQr(invitation) })
        addView(NisaaDesign.space(this@pairingCard, 0, 8))
        addView(NisaaDesign.outlineButton(this@pairingCard, getString(R.string.copy_invitation), R.drawable.ic_check) { copyInvitation(invitation) })
    } else {
        addView(NisaaDesign.space(this@pairingCard, 0, 14))
        addView(NisaaDesign.primaryButton(this@pairingCard, getString(R.string.create_invitation), R.drawable.ic_add) { createInvitation() })
    }
    addView(NisaaDesign.space(this@pairingCard, 0, 18))
    addView(NisaaDesign.divider(this@pairingCard))
    addView(NisaaDesign.space(this@pairingCard, 0, 14))
    addView(NisaaDesign.eyebrow(this@pairingCard, getString(R.string.accept_invitation)))
    val code = NisaaDesign.field(this@pairingCard, getString(R.string.pairing_code_hint))
    addView(code)
    addView(NisaaDesign.space(this@pairingCard, 0, 9))
    addView(NisaaDesign.secondaryButton(this@pairingCard, getString(R.string.pairing_confirm), R.drawable.ic_check) {
        acceptInvitation(code.text.toString())
    })
    addView(NisaaDesign.space(this@pairingCard, 0, 8))
    addView(NisaaDesign.outlineButton(this@pairingCard, getString(R.string.pairing_scan_qr), R.drawable.ic_share) {
        startPairingQrScan()
    })
    addView(NisaaDesign.space(this@pairingCard, 0, 10))
    addView(NisaaDesign.body(this@pairingCard, getString(R.string.pairing_not_configured)))
    addView(NisaaDesign.space(this@pairingCard, 0, 7))
    addView(NisaaDesign.body(this@pairingCard, getString(R.string.pairing_fallback)))
    addView(NisaaDesign.space(this@pairingCard, 0, 7))
    addView(NisaaDesign.body(this@pairingCard, getString(R.string.pairing_share_secure_note)))
}

private fun MainActivity.createInvitation() {
    val current = profile ?: return
    val invitation = container.relationshipPolicy.createInvitation(current.id, current.role)
    val result = repository.saveInvitation(invitation)
    if (result is DomainResult.Success) {
        lastInvitation = invitation
        showToast(getString(R.string.invitation_created))
        render()
    } else {
        showToast((result as DomainResult.Failure).error.message)
    }
}

private fun MainActivity.shareInvitation(invitation: PairingInvitation) {
    val payload = PairingPayload.fromInvitation(invitation)
    val message = getString(
        R.string.pairing_whatsapp_message,
        getString(R.string.app_name),
        invitation.token,
        payload.toUri(),
        PairingLinks.INSTALL_URL
    )
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text=" + Uri.encode(message)))
    try {
        startActivity(intent)
    } catch (_: Exception) {
        showToast(getString(R.string.pairing_fallback))
    }
}

private fun MainActivity.showPairingQr(invitation: PairingInvitation) {
    val payload = PairingPayload.fromInvitation(invitation)
    val image = ImageView(this).apply {
        setImageBitmap(PairingQrCode.create(payload.toUri(), NisaaDesign.dp(this@showPairingQr, 640)))
        adjustViewBounds = true
        contentDescription = getString(R.string.pairing_qr_title)
        setPadding(NisaaDesign.dp(this@showPairingQr, 18), NisaaDesign.dp(this@showPairingQr, 18), NisaaDesign.dp(this@showPairingQr, 18), NisaaDesign.dp(this@showPairingQr, 18))
    }
    AlertDialog.Builder(this)
        .setTitle(R.string.pairing_qr_title)
        .setMessage(R.string.pairing_qr_body)
        .setView(image)
        .setPositiveButton(R.string.close, null)
        .show()
}

private fun MainActivity.copyInvitation(invitation: PairingInvitation) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name), invitation.token))
    showToast(getString(R.string.copy_invitation))
}

private fun MainActivity.acceptInvitation(rawCode: String) {
    val current = profile ?: return
    val code = rawCode.trim()
    if (code.isBlank()) {
        showToast(getString(R.string.pairing_code_hint))
        return
    }
    val invitation = repository.findInvitation(code)
    if (invitation == null) {
        showToast(getString(R.string.pairing_not_configured))
        return
    }
    val relationshipId = UUID.randomUUID().toString()
    val permissions = RelationshipPermissions(
        relationshipId = relationshipId,
        permissions = container.relationshipPolicy.withPermissionDefaults(relationshipId)
    )
    val base = Relationship(
        id = relationshipId,
        status = RelationshipStatus.PENDING,
        permissions = permissions,
        connectionToken = code
    )
    val accepted = container.relationshipPolicy.acceptInvitation(invitation, current.id, current.role, base)
    if (accepted is DomainResult.Failure) {
        showToast(accepted.error.message)
        return
    }
    val result = repository.createRelationship((accepted as DomainResult.Success).value, current.id)
    if (result is DomainResult.Success) {
        repository.markInvitationConsumed(code)
        selectedRelationshipId = result.value.id
        lastInvitation = null
        showToast(getString(R.string.relationship_active))
        render()
    } else {
        showToast((result as DomainResult.Failure).error.message)
    }
}

private fun MainActivity.supportRequestDialog(relationship: Relationship) {
    val input = EditText(this).apply {
        hint = getString(R.string.support_message_hint)
        minHeight = NisaaDesign.dp(this@supportRequestDialog, 110)
        gravity = Gravity.TOP
        setPadding(NisaaDesign.dp(this@supportRequestDialog, 14), NisaaDesign.dp(this@supportRequestDialog, 10), NisaaDesign.dp(this@supportRequestDialog, 14), NisaaDesign.dp(this@supportRequestDialog, 10))
        background = NisaaDesign.rounded(this@supportRequestDialog, NisaaDesign.color(this@supportRequestDialog, R.color.nisaa_surface), NisaaDesign.color(this@supportRequestDialog, R.color.nisaa_line), 14)
    }
    AlertDialog.Builder(this)
        .setTitle(R.string.support_request)
        .setView(input)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.send_request) { _, _ ->
            val message = input.text.toString().trim()
            val current = profile
            if (message.isBlank()) {
                showToast(getString(R.string.support_message_hint))
            } else if (current != null) {
                val result = repository.addSupportRequest(
                    SupportRequest(relationshipId = relationship.id, senderUserId = current.id, message = message)
                )
                if (result is DomainResult.Success) showToast(getString(R.string.support_sent))
                else showToast((result as DomainResult.Failure).error.message)
            }
        }
        .show()
}

private fun MainActivity.confirmRevoke(relationship: Relationship) {
    AlertDialog.Builder(this)
        .setTitle(R.string.confirmation_title)
        .setMessage(R.string.confirmation_revoke)
        .setNegativeButton(R.string.cancel, null)
        .setPositiveButton(R.string.confirm_revoke) { _, _ ->
            val result = repository.revokeRelationship(relationship)
            if (result is DomainResult.Success) {
                showToast(getString(R.string.relationship_revoked))
                render()
            } else {
                showToast((result as DomainResult.Failure).error.message)
            }
        }
        .show()
}
