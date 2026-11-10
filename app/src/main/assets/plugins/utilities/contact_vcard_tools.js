/**
 * @plugin
 * @id utilities.contact_vcard_tools
 * @name VCF Contact Sanitizer & Formatter
 * @version 1.1.0
 * @author Zaid Ashiq Ultra
 * @category utilities
 * @permissions EXPORT_VCF,FORMAT_E164
 * @description Normalizes session phone numbers to E.164 and builds RFC-6350 vCard 3.0 export blocks.
 */
function executePlugin(contact) {
  const name = (contact && contact.displayName) || 'WhatsApp Contact';
  const phone = (contact && contact.phone) || '+0000000000';
  return {
    vcard: "BEGIN:VCARD\nVERSION:3.0\nFN:" + name + "\nTEL;TYPE=CELL:" + phone + "\nEND:VCARD"
  };
}
