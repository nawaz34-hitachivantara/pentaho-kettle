/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.core.encryption;

import org.eclipse.jetty.util.security.Password;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.support.encryption.PasswordEncoderException;

/**
 * This class handles basic encryption of passwords in Kettle. Note that it's not really encryption, it's more
 * obfuscation. Passwords are <b>difficult</b> to read, not impossible.
 *
 * @author Matt
 * @since 17-12-2003
 *
 */
public class Encr {

  private static TwoWayPasswordEncoderInterface encoder;

  public Encr() {
  }

  @Deprecated
  public boolean init() {
    return true;
  }

  public static void init( String encoderPluginId ) throws KettleException {
    if ( Utils.isEmpty( encoderPluginId ) ) {
      throw new KettleException( "Unable to initialize the two way password encoder: No encoder plugin type specified." );
    }
    PluginRegistry registry = PluginRegistry.getInstance();
    PluginInterface plugin = registry.findPluginWithId( TwoWayPasswordEncoderPluginType.class, encoderPluginId );
    if ( plugin == null ) {
      throw new KettleException( "Unable to find plugin with ID '" + encoderPluginId + "'.  If this is a test, make sure"
        + " kettle-core tests jar is a dependency.  If this is live make sure a kettle-password-encoder-plugins.xml"
        + " exits in the classpath" );
    }
    encoder = (TwoWayPasswordEncoderInterface) registry.loadClass( plugin );

    // Load encoder specific options...
    //
    try {
      encoder.init();
    } catch ( PasswordEncoderException e ) {
      throw new KettleException( e );
    }
  }

  /**
   * Old signature code, used in license keys, from the Kettle 1.x days.
   * @param signature
   * @param verify
   * @return
   * @deprecated
   */
  @Deprecated
  public static final boolean checkSignatureShort( String signature, String verify ) {
    return getSignatureShort( signature ).equalsIgnoreCase( verify );
  }

  /**
   * Old Kettle 1.x code used to get a short signature from a long version signature for license checking.
   * @param signature
   * @return
   * @deprecated
   */
  @Deprecated
  public static final String getSignatureShort( String signature ) {
    String retval = "";
    if ( signature == null ) {
      return retval;
    }
    int len = signature.length();
    if ( len < 6 ) {
      return retval;
    }
    retval = signature.substring( len - 5, len );

    return retval;
  }

  public static final String encryptPassword( String password ) {

    return encoder.encode( password, false );

  }

  public static final String decryptPassword( String encrypted ) {

    return encoder.decode( encrypted );

  }

  /**
   * The word that is put before a password to indicate an encrypted form. If this word is not present, the password is
   * considered to be NOT encrypted
   */
  @SuppressWarnings( "squid:S2068" ) public static final String PASSWORD_ENCRYPTED_PREFIX = "Encrypted ";

  /**
   * Encrypt the password, but only if the password doesn't contain any variables.
   *
   * @param password
   *          The password to encrypt
   * @return The encrypted password or the
   */
  public static final String encryptPasswordIfNotUsingVariables( String password ) {

    return encoder.encode( password, true );
  }

  /**
   * Decrypts a password if it contains the prefix "Encrypted "
   *
   * @param password
   *          The encrypted password
   * @return The decrypted password or the original value if the password doesn't start with "Encrypted "
   */
  public static final String decryptPasswordOptionallyEncrypted( String password ) {

    return encoder.decode( password, true );
  }

  /**
   * Create an encrypted password
   *
   * @param args
   *          the password to encrypt
   */
  public static void main( String[] args ) throws KettleException {
    KettleClientEnvironment.init();
    if ( args.length != 2 ) {
      printOptions();
      System.exit( 9 );
    }

    String option = args[0];
    String password = args[1];

    if ( Const.trim( option ).substring( 1 ).equalsIgnoreCase( "kettle" ) ) {
      // Kettle password obfuscation
      //
      try {
        String obfuscated = Encr.encryptPasswordIfNotUsingVariables( password );
        System.out.println( obfuscated );
        System.exit( 0 );
      } catch ( Exception ex ) {
        System.err.println( "Error encrypting password" );
        ex.printStackTrace();
        System.exit( 2 );
      }

    } else if ( Const.trim( option ).substring( 1 ).equalsIgnoreCase( "carte" ) ) {
      // Jetty password obfuscation
      //
      String obfuscated = Password.obfuscate( password );
      System.out.println( obfuscated );
      System.exit( 0 );

    } else {
      // Unknown option, print usage
      //
      System.err.println( "Unknown option '" + option + "'\n" );
      printOptions();
      System.exit( 1 );
    }

  }

  private static void printOptions() {
    System.err.println( "encr usage:\n" );
    System.err.println( "  encr <-kettle|-carte> <password>" );
    System.err.println( "  Options:" );
    System.err.println( "    -kettle: generate an obfuscated password to include in Kettle XML files" );
    System.err.println( "    -carte : generate an obfuscated password to include in the carte password file 'pwd/kettle.pwd'" );
    System.err.println( "\nThis command line tool obfuscates a plain text password for use in XML and password files." );
    System.err.println( "Make sure to also copy the '" + PASSWORD_ENCRYPTED_PREFIX + "' prefix to indicate the obfuscated nature of the password." );
    System.err.println( "Kettle will then be able to make the distinction between regular plain text passwords and obfuscated ones." );
    System.err.println();
  }
}
