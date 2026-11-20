package com.breadhardit.travelagencykata;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreationalPatternExercices {

    /*
     * Factory pattern exercise:
     * Accounts support deposits and withdrawals. The movement model must also
     * support transfers and annulments without adding movement-type conditionals
     * to Account.
     */

    interface Movement {
        String id();

        Account sourceAccount();

        void apply();
    }

    abstract static class ReversibleMovement implements Movement {
        private final String id = UUID.randomUUID().toString();
        private boolean applied;

        @Override
        public final String id() {
            return id;
        }

        @Override
        public final void apply() {
            if (applied) {
                throw new IllegalStateException("Movement has already been applied");
            }
            doApply();
            applied = true;
        }

        final void reverse() {
            if (!applied) {
                throw new IllegalStateException("Movement has not been applied or was already reversed");
            }
            doReverse();
            applied = false;
        }

        abstract void doApply();

        abstract void doReverse();
    }

    static class Account {
        private final String id;
        private long balance;
        private final Map<String, Movement> movements = new HashMap<>();

        Account(String id) {
            this.id = id;
        }

        String id() {
            return id;
        }

        long balance() {
            return balance;
        }

        void applyMovement(Movement movement) {
            if (movement.sourceAccount() != this) {
                throw new IllegalArgumentException("Movement belongs to another source account");
            }
            if (movements.containsKey(movement.id())) {
                throw new IllegalStateException("Movement is already registered");
            }
            movement.apply();
            movements.put(movement.id(), movement);
        }

        private void credit(long amount) {
            balance += amount;
        }

        private void debit(long amount) {
            if (balance < amount) {
                throw new IllegalStateException("Insufficient funds");
            }
            balance -= amount;
        }
    }

    static class DepositMovement extends ReversibleMovement {
        private final Account account;
        private final long amount;

        DepositMovement(Account account, long amount) {
            this.account = account;
            this.amount = requirePositiveAmount(amount);
        }

        @Override
        public Account sourceAccount() {
            return account;
        }

        @Override
        void doApply() {
            account.credit(amount);
        }

        @Override
        void doReverse() {
            account.debit(amount);
        }
    }

    static class WithdrawalMovement extends ReversibleMovement {
        private final Account account;
        private final long amount;

        WithdrawalMovement(Account account, long amount) {
            this.account = account;
            this.amount = requirePositiveAmount(amount);
        }

        @Override
        public Account sourceAccount() {
            return account;
        }

        @Override
        void doApply() {
            account.debit(amount);
        }

        @Override
        void doReverse() {
            account.credit(amount);
        }
    }

    static class TransferMovement extends ReversibleMovement {
        private final Account source;
        private final Account destination;
        private final long amount;

        TransferMovement(Account source, Account destination, long amount) {
            if (source == destination) {
                throw new IllegalArgumentException("Source and destination accounts must differ");
            }
            this.source = source;
            this.destination = destination;
            this.amount = requirePositiveAmount(amount);
        }

        @Override
        public Account sourceAccount() {
            return source;
        }

        @Override
        void doApply() {
            source.debit(amount);
            destination.credit(amount);
        }

        @Override
        void doReverse() {
            destination.debit(amount);
            source.credit(amount);
        }
    }

    static class AnnulmentMovement extends ReversibleMovement {
        private final ReversibleMovement originalMovement;

        AnnulmentMovement(ReversibleMovement originalMovement) {
            this.originalMovement = originalMovement;
        }

        @Override
        public Account sourceAccount() {
            return originalMovement.sourceAccount();
        }

        @Override
        void doApply() {
            originalMovement.reverse();
        }

        @Override
        void doReverse() {
            originalMovement.apply();
        }
    }

    static class MovementFactory {
        private MovementFactory() {
        }

        static Movement deposit(Account account, long amount) {
            return new DepositMovement(account, amount);
        }

        static Movement withdrawal(Account account, long amount) {
            return new WithdrawalMovement(account, amount);
        }

        static Movement transfer(Account source, Account destination, long amount) {
            return new TransferMovement(source, destination, amount);
        }

        static Movement annulment(Movement originalMovement) {
            if (!(originalMovement instanceof ReversibleMovement reversibleMovement)) {
                throw new IllegalArgumentException("Movement cannot be annulled");
            }
            return new AnnulmentMovement(reversibleMovement);
        }
    }

    private static long requirePositiveAmount(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Movement amount must be positive");
        }
        return amount;
    }

    @Test
    void createsAndAppliesDepositsAndWithdrawals() {
        Account account = new Account("account-1");

        account.applyMovement(MovementFactory.deposit(account, 1_000));
        account.applyMovement(MovementFactory.withdrawal(account, 150));

        assertEquals(850, account.balance());
    }

    @Test
    void transfersFundsBetweenAccountsAndCanAnnulTheTransfer() {
        Account source = new Account("source");
        Account destination = new Account("destination");
        source.applyMovement(MovementFactory.deposit(source, 1_000));

        Movement transfer = MovementFactory.transfer(source, destination, 300);
        source.applyMovement(transfer);

        assertEquals(700, source.balance());
        assertEquals(300, destination.balance());

        source.applyMovement(MovementFactory.annulment(transfer));

        assertEquals(1_000, source.balance());
        assertEquals(0, destination.balance());
    }

    @Test
    void annulsAWithdrawalAndPreventsAnnulmentTwice() {
        Account account = new Account("account-1");
        account.applyMovement(MovementFactory.deposit(account, 500));
        Movement withdrawal = MovementFactory.withdrawal(account, 200);
        account.applyMovement(withdrawal);

        account.applyMovement(MovementFactory.annulment(withdrawal));

        assertEquals(500, account.balance());
        assertThrows(IllegalStateException.class,
                () -> account.applyMovement(MovementFactory.annulment(withdrawal)));
    }

    @Test
    void rejectsInvalidAmountsAndWithdrawalsWithoutFunds() {
        Account account = new Account("account-1");

        assertThrows(IllegalArgumentException.class,
                () -> MovementFactory.deposit(account, 0));
        assertThrows(IllegalStateException.class,
                () -> account.applyMovement(MovementFactory.withdrawal(account, 10)));
        assertEquals(0, account.balance());
    }
}
