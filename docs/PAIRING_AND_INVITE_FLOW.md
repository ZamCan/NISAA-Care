# NISAA CARE Pairing & Invite Flow

NISAA CARE supports three user-controlled pairing channels:

1. Short pairing code.
2. Nearby QR — either side can display and the other can scan.
3. WhatsApp — an explicit user action sends a temporary pairing link plus an install link.

All three channels converge on the same relationship authorization flow.

## Security boundary

QR/deep-link payloads contain only a protocol version, random short-lived token, inviter role and expiry. Never encode menstrual dates, fertility estimates, symptoms, pregnancy status, private notes, phone numbers, names or permissions.

The backend must validate token expiry, role compatibility, replay protection and final relationship creation.

## Pairing code

Use a human-readable, short-lived code. Show its remaining validity and regenerate after expiry. Rate-limit failed attempts. Never use a phone number as a pairing code.

## QR

Either side can:
Display my QR -> other person scans -> verify -> accept -> choose permissions -> connected.

Or:
Scan QR -> verify -> accept -> choose permissions -> connected.

The scanner is a platform boundary so the camera/QR implementation can change without changing domain authorization.

## WhatsApp

The user explicitly chooses WhatsApp. The message contains a temporary pairing link and a Google Play install link for com.zamcan.nisaacare. If the recipient does not have NISAA CARE, the install link gives them a path to install it, then they can reopen the invitation.

No automatic WhatsApp message is sent.

## Install fallback

The production pairing URL should eventually be an Android App Link/HTTPS entry point backed by a small public landing page. The fallback should explain: install NISAA CARE -> reopen the invitation -> review -> accept -> choose permissions.

Until a public pairing domain exists, the native nisaacare://pair path works for installed devices and the Play Store URL is the explicit installation route.

## Relationship authorization

All channels call RelationshipPolicy.acceptInvitation(...). Permissions default to OFF. Wife controls sharing. Revocation increments versions. Multiple-wife relationships remain isolated by relationship ID. Health data never enters pairing payloads.

## Native UX

Connect spouse -> Pairing code / Scan QR / Show QR / WhatsApp -> verify -> accept -> sharing permissions -> connected.

The screen should use compact native rows, clear iconography, one primary action, expiry state and explicit privacy language rather than a web-form layout.
