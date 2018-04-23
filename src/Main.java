import java.math.BigInteger;
import java.security.*;

public class Main {

    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException {
        /*
         * Generate key pairs, for Scrooge & Alice
         */
        KeyPair pk_scrooge = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        KeyPair pk_alice = KeyPairGenerator.getInstance("RSA").generateKeyPair();

        /*
         * Set up the root transaction
         */
        Tx tx = new Tx();
        tx.addOutput(10, pk_scrooge.getPublic());

        byte[] initialHash = BigInteger.valueOf(0).toByteArray();
        tx.addInput(initialHash, 0);

        tx.signTx(pk_scrooge.getPrivate(), 0);
        /*
         * Set up the UTXPool
         */
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(tx.getHash(), 0);
        utxoPool.addUTXO(utxo, tx.getOutput(0));


        /*
         * Set up a test Transaction
         */
        Tx tx2 = new Tx();
        tx2.addInput(tx.getHash(), 0);

        tx2.addOutput(5, pk_alice.getPublic());
        tx2.addOutput(3, pk_alice.getPublic());
        tx2.addOutput(2, pk_alice.getPublic());

        tx2.signTx(pk_scrooge.getPrivate(), 0);

        /*
         * Start the test
         */
        TxHandler txHandler = new TxHandler(utxoPool);
        System.out.println("txHandler.isValidTx(tx2) returns: " + txHandler.isValidTx(tx2));
        System.out.println("txHandler.isValidTx(new Transaction[]{tx2}) returns: " +
            txHandler.handleTxs(new Transaction[]{tx2}).length + " transaction(s)");


    }

    public static class Tx extends  Transaction {
        public void signTx(PrivateKey sk, int input) throws SignatureException {
            Signature sig = null;
            try {
                sig = Signature.getInstance("SHA256withRSA");
                sig.initSign(sk);
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            this.addSignature(sig.sign(), input);
            this.finalize();
        }
    }
}


