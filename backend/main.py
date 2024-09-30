
import base64
from typing import Dict
from flask import Flask, logging, request
from webauthn import generate_authentication_options, generate_registration_options, options_to_json, verify_authentication_response, verify_registration_response
from webauthn.helpers.structs import (
    AuthenticatorSelectionCriteria,
    UserVerificationRequirement,
    ResidentKeyRequirement,
    AuthenticatorAttachment
)
from webauthn.helpers.exceptions import InvalidAuthenticationResponse, InvalidRegistrationResponse

# HOST the .well-known/origin.json file inside your public domain. Check https://developer.android.com/identity/sign-in/credential-manager#add-support-dal
RP_ID = "inusedname.github.io"
# Get the sha-256 hash of android studio. Should match with origin.json. Check https://developer.android.com/identity/sign-in/credential-manager#verify-origin
EXPECTED_ORIGIN = "android:apk-key-hash:-------------------"

app = Flask(__name__)
logger = logging.create_logger(app)
users = {}
# no timeout, revoke after check 1 time
challenges: Dict[str, bytes] = {}

@app.route('/get-register-options', methods=['GET'])
def get_register_options():
    username = request.args.get('username')
    passkeyRequestJson = generate_registration_options(
        rp_id=RP_ID,
        rp_name="WebAuthn Demo",
        user_name=username,
        authenticator_selection=AuthenticatorSelectionCriteria(
            authenticator_attachment=AuthenticatorAttachment.PLATFORM,
            resident_key=ResidentKeyRequirement.REQUIRED,
            user_verification=UserVerificationRequirement.REQUIRED,
            require_resident_key=True
        )
    )
    challenges[username] = passkeyRequestJson.challenge
    json = options_to_json(passkeyRequestJson)
    b64 = base64.b64encode(json.encode('utf-8'))
    return b64


@app.route('/verify-registration', methods=['POST'])
def verify_registration():
    username = request.args.get('username')
    body = request.get_json(force=True)
    try:
        result = verify_registration_response(
            credential=body,
            expected_challenge=challenges[username],
            expected_origin=EXPECTED_ORIGIN,
            expected_rp_id=RP_ID,
        )
        logger.info(f"User {username} have register ID: {result.credential_id}")
        users[username] = result.credential_public_key
        return "Registration successful"
    except InvalidRegistrationResponse as e:
        logger.error(e, exc_info=True)
        return "Invalid passkey", 400
    except Exception as e:
        logger.error(e, exc_info=True)
        return "Cannot verify you", 500
    finally:
        challenges.pop(username, None)


@app.route('/get-authentication-options', methods=['GET'])
def get_authentication_options():
    username = request.args.get('username')
    passkeyRequestJson = generate_authentication_options(
        rp_id=RP_ID,
        user_verification=UserVerificationRequirement.REQUIRED,
    )
    challenges[username] = passkeyRequestJson.challenge
    json = options_to_json(passkeyRequestJson)
    b64 = base64.b64encode(json.encode('utf-8'))
    return b64

@app.route('/verify-authentication', methods=['POST'])
def verify_authentication():
    username = request.args.get('username')
    body = request.get_json(force=True)
    try:
        verify_authentication_response(
            credential=body,
            expected_challenge=challenges[username],
            expected_origin=EXPECTED_ORIGIN,
            expected_rp_id=RP_ID,
            credential_public_key=users[username],
            credential_current_sign_count=0
        )        
        return "Authentication successful"
    except InvalidAuthenticationResponse as e:
        logger.error(e, exc_info=True)
        return "Invalid passkey", 401
    except Exception as e:
        logger.error(e, exc_info=True)
        return "Unknown exception", 500
    finally:
        challenges.pop(username, None)

if __name__ == '__main__':
    app.run(host="localhost", port=8000, debug=True)
